package com.ds.analytics;

import java.rmi.RemoteException;
import java.util.HashMap;

import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class AnalyticsServer implements Analytics {

    private final HashMap<String, Subscription> subscriptions =
            new HashMap<String, Subscription>();

    @Override
    public String subscribe(String regex, EventProcessor callback) throws RemoteException {
        Subscription subscription;
        String id;

        synchronized (subscriptions) {
            /* Just to be safe, check for collisions. */
            do {
                subscription = new Subscription(regex, callback);
                id = subscription.getUuid().toString();
            } while (subscriptions.containsKey(id));

            subscriptions.put(id, subscription);
        }

        Log.d("New subscription added; ID: %s, Pattern: %s", id, regex);
        return id;
    }

    @Override
    public void processEvent(Event event) throws RemoteException {
        Log.d("processEvent: %s", event.getType());

        /* Generate derived events. */

        /* Notify subscribers. */

    }

    @Override
    public void unsubscribe(String subscriptionID) throws RemoteException {
        Subscription result;

        synchronized (subscriptionID) {
            result = subscriptions.remove(subscriptionID);
        }

        if (result != null) {
            Log.d("Removed subscribtion; ID: %s", subscriptionID);
        } else {
            Log.d("Received unsubscription request for nonexistent ID: %s", subscriptionID);
        }
    }

}
