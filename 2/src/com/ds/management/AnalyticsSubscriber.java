package com.ds.management;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import com.ds.analytics.Analytics;
import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class AnalyticsSubscriber implements EventProcessor {

    private final Analytics analytics;
    private final EventProcessor stub;
    private PrintMode mode = PrintMode.HIDE;
    private final List<String> printCache = new ArrayList<String>();

    public enum PrintMode {
        AUTO,
        HIDE
    }

    public AnalyticsSubscriber(String analyticsBindingName) throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();
        analytics = (Analytics)registry.lookup(analyticsBindingName);
        stub = (EventProcessor)UnicastRemoteObject.exportObject(this, 0);
    }

    public String subscribe(String filter) {
        try {
            return analytics.subscribe(filter, stub);
        } catch (RemoteException e) {
            Log.e(e.getMessage());
            return null;
        }
    }

    public void unsubscribe(String subscriptionID) {
        try {
            analytics.unsubscribe(subscriptionID);
        } catch (RemoteException e) {
            Log.e(e.getMessage());
        }
    }

    public void setMode(PrintMode mode) {
        this.mode = mode;
    }

    public synchronized void print() {
        for (String s : printCache) {
            System.out.println(s);
        }
        printCache.clear();
    }

    public synchronized void shutdown() {
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            Log.e(e.getMessage());
        }
    }

    @Override
    public synchronized void processEvent(Event event) throws RemoteException {
        Log.d("Received event: %s", event.getType());
        printCache.add(event.toString());
        if (mode == PrintMode.AUTO) {
            print();
        }
    }

}
