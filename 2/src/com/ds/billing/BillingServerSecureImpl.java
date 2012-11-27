package com.ds.billing;

import java.rmi.RemoteException;

import com.ds.loggers.Log;

class BillingServerSecureImpl implements BillingServerSecure {
    private final PriceStepStorage priceStepStorage = PriceStepStorage.getInstance();
    private final BillStorage billStorage = BillStorage.getInstance();

    private static BillingServerSecureImpl instance;

    static synchronized BillingServerSecureImpl getInstance() {
        if (instance == null) {
            instance = new BillingServerSecureImpl();
        }

        return instance;
    }

    private BillingServerSecureImpl() { }

    public PriceSteps getPriceSteps() throws RemoteException {
        return priceStepStorage.getPriceSteps();
    }

    public void createPriceStep(double startPrice, double endPrice, double fixedPrice,
            double variablePricePercent) throws RemoteException {
        String errMsg = null;

        try {
            if (!priceStepStorage.insert(new PriceStep(startPrice, endPrice, fixedPrice,
                                            variablePricePercent))) {
                errMsg = "Step overlapping. Remove old step first.";
            }
        } catch (IllegalArgumentException e) {
            errMsg = e.getLocalizedMessage();
        }

        if (errMsg != null) {
            throw new RemoteException("ERROR: " + errMsg);
        }
    }

    public void deletePriceStep(double startPrice, double endPrice)
            throws RemoteException {
        if (!priceStepStorage.delete(startPrice, endPrice)) {
            throw new RemoteException(String.format(
                                    "ERROR: Price step [%.2f %.2f] does not exist",
                                    startPrice, endPrice));
        }
    }

    public void billAuction(String user, long auctionID, double price)
            throws RemoteException {
        Log.d("billing auction "+auctionID+" from "+user+" "+price);
        PriceStep p = priceStepStorage.getPriceStepForPrice(price);
        if (p == null) {
            Log.d(String.format("ERROR: No price step for %.2f", price));
            throw new RemoteException(String.format("ERROR: No price step for %.2f", price));
        }

        billStorage.insert(user, new AuctionBill(auctionID, price,
                    priceStepStorage.getPriceStepForPrice(price)));
    }

    public Bill getBill(String user) throws RemoteException {
        return billStorage.getBill(user);
    }
}
