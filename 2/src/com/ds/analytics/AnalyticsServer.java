package com.ds.analytics;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ds.event.Event;
import com.ds.interfaces.EventProcessor;
import com.ds.loggers.Log;

public class AnalyticsServer implements Analytics {

    private final Subscriptions subs = new Subscriptions();
    private final AnalyticsData data = new AnalyticsData();

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

        List<Event> generatedEvents = data.updateAndGenerateEvents(event);

        /* Notify subscribers. */

        subs.notifySubscribers(event);
        for (Event e : generatedEvents) {
            subs.notifySubscribers(e);
        }

    }

    private static class AnalyticsData {

        private static final long MSECS_PER_SEC = 1000;
        private static final long SECS_PER_MIN = 60;

        private final int sessionSecondsMin = -1;
        private final int sessionSecondsMax = -1;
        private final int sessionSecondsAccumulated = 0;
        private final int sessionCount = 0;

        private final double bidPriceMax = -1;
        private final int bidCount = 0;

        private final int auctionSecondsAccumulated = 0;
        private final int auctionCount = 0;
        private final int auctionSuccessCount = 0;

        private final Date runningSince;


        public AnalyticsData() {
            runningSince = new Date();
        }

        public List<Event> updateAndGenerateEvents(Event event) {

            /* TODO: keep track of users, auctions, and generate events accordingly. */

            return new ArrayList<Event>();
        }

        private int getSessionSecondsMin() {
            return sessionSecondsMin;
        }

        private int getSessionSecondsMax() {
            return sessionSecondsMax;
        }

        private double getSessionSecondsAvg() {
            return (double)sessionSecondsAccumulated / sessionCount;
        }

        private double getBidPriceMax() {
            return bidPriceMax;
        }

        private double getBidCountPerMinute() {
            Date now = new Date();
            long seconds = (now.getTime() - runningSince.getTime()) / MSECS_PER_SEC;

            if (seconds == 0) {
                return 0;
            }

            return (bidCount * SECS_PER_MIN) / (double)seconds;
        }

        private double getAuctionSecondsAvg() {
            return (double)auctionSecondsAccumulated / auctionCount;
        }

        private double getAuctionSuccessRatio() {
            return (double)auctionSuccessCount / auctionCount;
        }
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

            /* Notify all matching subscribers. */

            for (Subscription sub : subsByCallback.values()) {
                try {
                    if (sub.matches(event)) {
                        sub.notifySubscriber(event);
                    }
                } catch (RemoteException e) {
                    toRemove.add(sub);
                }
            }

            /* Remove lost subscriptions. */

            for (Subscription s : toRemove) {
                for (String id : s.getSubscriptionIDs()) {
                    subsByID.remove(id);
                }
                subsByCallback.remove(s.getCallback());
            }
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

            public Set<String> getSubscriptionIDs() {
                return patterns.keySet();
            }
        }
    }
}
