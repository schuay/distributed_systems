package com.ds.server;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.ds.billing.BillingServer;
import com.ds.billing.BillingServerSecure;
import com.ds.interfaces.BillingListener;
import com.ds.loggers.Log;
import com.ds.util.RegistryProperties;

/**
 * Forwards events to an event processor.
 */
public class BillingForwarder implements BillingListener {

    private final BillingServerSecure billing;

    public BillingForwarder(String bindingName) throws NotBoundException, IOException {
        RegistryProperties props = new RegistryProperties();
        Registry registry = LocateRegistry.getRegistry(props.getHost(), props.getPort());
        BillingServer bs = (BillingServer)registry.lookup(bindingName);

        /* TODO do this nicely */
        billing = bs.login("auctionsRus", "auctionsRus");
    }

    @Override
    public void billAuction(String user, long auctionID, double price) {
        try {
            billing.billAuction(user, auctionID, price);
        } catch (RemoteException e) {
            /* TODO */
            Log.e(e.getMessage());
        }
    }

}
