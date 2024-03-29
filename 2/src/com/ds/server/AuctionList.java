
package com.ds.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.ds.event.AuctionEvent;
import com.ds.event.Event;
import com.ds.interfaces.BillingListener;
import com.ds.interfaces.EventListener;
import com.ds.loggers.Log;
import com.ds.server.UserList.User;

public class AuctionList implements EventListener, BillingListener {

    private final ConcurrentHashMap<Integer, Auction> auctions = new ConcurrentHashMap<Integer, Auction>();
    private final Timer timer = new Timer();
    private int id = 0;
    private final List<EventListener> listeners = new ArrayList<EventListener>();
    private final List<BillingListener> billingListeners = new ArrayList<BillingListener>();

    public synchronized int add(String description, User owner, Date end) {
        int auctionId = id++;

        notifyListeners(new AuctionEvent(AuctionEvent.AUCTION_STARTED, auctionId));
        Auction auction = new Auction(auctionId, description, owner, end);
        auction.addOnEventListener(this);
        auction.addBillingListener(this);
        auctions.put(auctionId, auction);
        timer.schedule(new AuctionTimerTask(this, auctionId), end);

        return auctionId;
    }

    public synchronized void bid(int auctionId, User bidder, int amount) {
        if (!auctions.containsKey(auctionId)) {
            Log.e("No such auction");
            return;
        }
        auctions.get(auctionId).bid(bidder, amount);
    }

    private synchronized void expire(int auctionId) {
        notifyListeners(new AuctionEvent(AuctionEvent.AUCTION_ENDED, auctionId));
        auctions.get(auctionId).end();
        auctions.remove(auctionId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Auction auction : auctions.values()) {
            sb.append(String.format("%s%n", auction.toString()));
        }
        return sb.toString();
    }

    public void cancelTimers() {
        timer.cancel();
    }

    public void addOnEventListener(EventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeOnEventListener(EventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(Event event) {
        synchronized (listeners) {
            for (EventListener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }

    @Override
    public void onEvent(Event event) {
        notifyListeners(event);
    }

    public void addBillingListener(BillingListener billingListener) {
        synchronized (billingListeners) {
            billingListeners.add(billingListener);
        }
    }

    public void removeBillingListener(BillingListener billingListener) {
        synchronized (billingListeners) {
            billingListeners.remove(billingListener);
        }
    }

    private void notifyBillingListeners(String user, long auctionID, double price) {
        synchronized (billingListeners) {
            for (BillingListener billingListener : billingListeners) {
                billingListener.billAuction(user, auctionID, price);
            }
        }
    }

    @Override
    public void billAuction(String user, long auctionID, double price) {
        notifyBillingListeners(user, auctionID, price);
    }

    /**
     * Responsible for ending an auction once it expires.
     */
    private static class AuctionTimerTask extends TimerTask {

        private final int id;
        private final AuctionList list;

        public AuctionTimerTask(AuctionList list, int id) {
            this.list = list;
            this.id = id;
        }

        @Override
        public void run() {
            list.expire(id);
        }
    }
}
