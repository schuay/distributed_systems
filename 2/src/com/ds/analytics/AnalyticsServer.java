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

import com.ds.event.AuctionEvent;
import com.ds.event.BidEvent;
import com.ds.event.Event;
import com.ds.event.StatisticsEvent;
import com.ds.event.UserEvent;
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

        List<Event> gen = data.processEvent(event);

        /* Notify subscribers. */

        subs.notifySubscribers(event);
        for (Event e : gen) {
            subs.notifySubscribers(e);
        }

    }

    private static class AnalyticsData {

        private static final long MSECS_PER_SEC = 1000;
        private static final long SECS_PER_MIN = 60;

        private int sessionSecondsMin = -1;
        private int sessionSecondsMax = -1;
        private int sessionSecondsAccumulated = 0;
        private int sessionCount = 0;

        private double bidPriceMax = -1;
        private int bidCount = 0;

        private int auctionSecondsAccumulated = 0;
        private int auctionCount = 0;
        private int auctionSuccessCount = 0;

        private final Date runningSince;

        /** Tracks start times of currently active auctions. */
        private final HashMap<Long, Date> auctionsSince = new HashMap<Long, Date>();

        /** Tracks start times of currently active user sessions. */
        private final HashMap<String, Date> sessionsSince = new HashMap<String, Date>();


        public AnalyticsData() {
            runningSince = new Date();
        }

        public synchronized List<Event> processEvent(Event event) {
            List<Event> gen = new ArrayList<Event>();
            Date now = new Date();

            String t = event.getType();
            if (t.equals(AuctionEvent.AUCTION_STARTED)) {
                auctionsSince.put(((AuctionEvent)event).getAuctionID(), now);
            } else if (t.equals(AuctionEvent.AUCTION_ENDED)) {
                Date since = auctionsSince.remove(((AuctionEvent)event).getAuctionID());

                auctionCount++;
                auctionSecondsAccumulated += secondsBetween(since, now);

                gen.add(new StatisticsEvent(StatisticsEvent.AUCTION_TIME_AVG, getAuctionSecondsAvg()));
                gen.add(new StatisticsEvent(StatisticsEvent.AUCTION_SUCCESS_RATIO, getAuctionSuccessRatio()));
            } else if (t.equals(UserEvent.USER_LOGIN)) {
                sessionsSince.put(((UserEvent)event).getUserName(), now);
            } else if (t.equals(UserEvent.USER_LOGOUT)) {
                Date since = sessionsSince.remove(((UserEvent)event).getUserName());
                int sessionDuration = secondsBetween(since, now);

                sessionCount++;
                sessionSecondsAccumulated += sessionDuration;
                sessionSecondsMin = Math.min(sessionDuration, sessionSecondsMin);
                sessionSecondsMax = Math.max(sessionDuration, sessionSecondsMax);

                gen.add(new StatisticsEvent(StatisticsEvent.USER_SESSIONTIME_MIN, getSessionSecondsMin()));
                gen.add(new StatisticsEvent(StatisticsEvent.USER_SESSIONTIME_AVG, getSessionSecondsAvg()));
                gen.add(new StatisticsEvent(StatisticsEvent.USER_SESSIONTIME_MAX, getSessionSecondsMax()));
            } else if (t.equals(BidEvent.BID_PLACED)) {
                BidEvent be = (BidEvent)event;

                bidCount++;
                bidPriceMax = Math.max(be.getPrice(), bidPriceMax);

                gen.add(new StatisticsEvent(StatisticsEvent.BID_PRICE_MAX, getBidPriceMax()));
                gen.add(new StatisticsEvent(StatisticsEvent.BID_COUNT_PER_MINUTE, getBidCountPerMinute()));
            } else if (t.equals(BidEvent.BID_WON)) {
                auctionSuccessCount++;

                gen.add(new StatisticsEvent(StatisticsEvent.AUCTION_SUCCESS_RATIO, getAuctionSuccessRatio()));
            }

            return gen;
        }

        private static int secondsBetween(Date since, Date now) {
            return (int)((now.getTime() - since.getTime()) / MSECS_PER_SEC);
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
            int seconds = secondsBetween(runningSince,  now);

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
