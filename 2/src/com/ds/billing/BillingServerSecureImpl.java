package com.ds.billing;

import java.rmi.RemoteException;

public class BillingServerSecureImpl implements BillingServerSecure {

    public PriceSteps getPriceSteps() throws RemoteException {
        return null;
    }

    public void createPriceStep(double startPrice, double endPrice, double fixedPrice,
            double variablePricePercent) throws RemoteException {
    }

    public void deletePriceStep(double startPrice, double endPrice)
            throws RemoteException {
    }

    public void billAuction(String user, long auctionID, double price)
            throws RemoteException {
    }

    public Bill getBill(String user) throws RemoteException {
        return null;
    }
}
