package com.ds.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.ds.analytics.Analytics;
import com.ds.event.Event;
import com.ds.interfaces.EventListener;
import com.ds.loggers.Log;

/**
 * Forwards events to an event processor.
 */
public class EventForwarder implements EventListener {

    private final Analytics analytics;

    public EventForwarder(String bindingName) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        analytics = (Analytics)registry.lookup(bindingName);
    }

    @Override
    public void onEvent(Event event) {
        try {
            analytics.processEvent(event);
        } catch (RemoteException e) {
            /* TODO */
            Log.e(e.getMessage());
        }
    }

}
