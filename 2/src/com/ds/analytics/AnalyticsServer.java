package com.ds.analytics;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class AnalyticsServer implements Analytics {

    private final Subscriptions subs = new Subscriptions();

    @Override
    public String subscribe(String regex, EventProcessor callback) throws RemoteException {
        String id = subs.add(regex, callback);
        Log.d("New subscription added; ID: %s, Pattern: %s", id, regex);
        return id;
    }

    @Override
    public void processEvent(Event event) throws RemoteException {
        Log.d("processEvent: %s", event.getType());

        /* Generate derived events. */

        /* TODO */

        /* Notify subscribers. */

        subs.notifySubscribers(event);

    }

    @Override
    public void unsubscribe(String subscriptionID) throws RemoteException {
        subs.remove(subscriptionID);
    }

    /**
     * Stores a collection of subscriptions.
     * Subscriptions are handled in a callback-centric way internally; meaning
     * that one callback only has a single Subscription object, even if that
     * callback has multiple associated subscriptions (= filter regex). This is
     * done to send events only once, even if they match multiple subscriptions of
     * one callback.
     */
    private static class Subscriptions {

        /* This table can store the same subscription object multiple times, once
         * for each subscription id. */
        private final HashMap<String, Subscription> subsByID =
                new HashMap<String, Subscription>();

        private final HashMap<EventProcessor, Subscription> subsByCallback =
                new HashMap<EventProcessor, Subscription>();

        public synchronized String add(String regex, EventProcessor callback) {
            Subscription sub = subsByCallback.get(callback);
            if (sub == null) {
                sub = new Subscription(callback);
                subsByCallback.put(callback, sub);
            }

            String id = sub.add(regex);
            subsByID.put(id, sub);

            return id;
        }

        public synchronized void notifySubscribers(Event event) {
            List<Subscription> toRemove = new ArrayList<Subscription>();

            for (Subscription sub : subsByCallback.values()) {
                try {
                    if (sub.matches(event)) {
                        sub.notifySubscriber(event);
                    }
                } catch (RemoteException e) {
                    toRemove.add(sub);
                }
            }

            /* TODO: Remove lost subscriptions. */
        }

        public synchronized void remove(String id) {
            Subscription sub = subsByID.get(id);
            if (sub == null) {
                return;
            }

            sub.remove(id);
            subsByID.remove(id);

            if (sub.isEmpty()) {
                subsByCallback.remove(sub.getCallback());
            }
        }

        /**
         * A subscription object manages all subscriptions for a single client.
         */
        private static class Subscription {

            private final HashMap<String, Pattern> patterns;
            private final EventProcessor callback;

            public Subscription(EventProcessor callback) {
                this.patterns = new HashMap<String, Pattern>();
                this.callback = callback;
            }

            public void notifySubscriber(Event event) throws RemoteException {
                callback.processEvent(event);
            }

            public boolean matches(Event event) {
                for (Pattern pattern : patterns.values()) {
                    Matcher matcher = pattern.matcher(event.getType());
                    if (matcher.matches()) {
                        return true;
                    }
                }
                return false;
            }

            public String add(String regex) {
                String id = UUID.randomUUID().toString();
                patterns.put(id, Pattern.compile(regex));
                return id;
            }

            public void remove(String id) {
                patterns.remove(id);
            }

            public boolean isEmpty() {
                return patterns.isEmpty();
            }

            public EventProcessor getCallback() {
                return callback;
            }
        }
    }
}
