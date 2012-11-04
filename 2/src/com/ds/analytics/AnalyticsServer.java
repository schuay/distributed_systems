package com.ds.analytics;

import java.rmi.RemoteException;

import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class AnalyticsServer implements Analytics {

    @Override
    public String subscribe(String filter, EventProcessor callback) throws RemoteException {
        Log.d("subscribe");
        return null;
    }

    @Override
    public void processEvent(Event event) throws RemoteException {
        Log.d("processEvent");
    }

    @Override
    public void unsubscribe(String subscriptionID) throws RemoteException {
        Log.d("unsubscribe");
    }

}
