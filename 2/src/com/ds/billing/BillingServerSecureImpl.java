package com.ds.billing;

import java.rmi.RemoteException;

public class BillingServerSecureImpl implements BillingServerSecure {
    private final PriceStepStorage storage = PriceStepStorage.getInstance();

    public PriceSteps getPriceSteps() throws RemoteException {
        return storage.getPriceSteps();
    }

    public void createPriceStep(double startPrice, double endPrice, double fixedPrice,
            double variablePricePercent) throws RemoteException {
        String errMsg = null;

        try {
            if (!storage.insert(new PriceStep(startPrice, endPrice, fixedPrice,
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
        if (!PriceStepStorage.getInstance().delete(startPrice, endPrice)) {
            throw new RemoteException(String.format(
                                    "ERROR: Price step [%f %f] does not exist",
                                    startPrice, endPrice));
        }
    }

    public void billAuction(String user, long auctionID, double price)
            throws RemoteException {
    }

    public Bill getBill(String user) throws RemoteException {
        return null;
    }
}
