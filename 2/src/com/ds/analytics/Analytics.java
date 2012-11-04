package com.ds.analytics;

import java.rmi.RemoteException;

import com.ds.interfaces.EventProcessor;

public interface Analytics extends EventProcessor {

    String subscribe(String filter, EventProcessor callback) throws RemoteException;
    void unsubscribe(String subscriptionID) throws RemoteException;
}
