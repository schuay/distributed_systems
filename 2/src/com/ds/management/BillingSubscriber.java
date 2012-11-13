package com.ds.management;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.ds.billing.BillingServer;
import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class BillingSubscriber implements EventProcessor {

    private final BillingServer billing;
    private final EventProcessor stub;

    public BillingSubscriber(String billingBindingName) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        billing = (BillingServer)registry.lookup(billingBindingName);
        stub = (EventProcessor)UnicastRemoteObject.exportObject(this, 0);
    }

    @Override
    public void processEvent(Event event) throws RemoteException {
        // TODO Auto-generated method stub
    }

    public synchronized void shutdown() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            Log.e(e.getMessage());
        }
    }

}
