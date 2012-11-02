
package com.ds.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.ds.event.AuctionEvent;
import com.ds.event.Event;

public class AuctionList {

    private final ConcurrentHashMap<Integer, Auction> auctions = new ConcurrentHashMap<Integer, Auction>();
    private final Timer timer = new Timer();
    private int id = 0;
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    public synchronized int add(String description, User owner, Date end) {
        int auctionId = id++;

        notifyListeners(new AuctionEvent(AuctionEvent.AUCTION_STARTED, auctionId));
        auctions.put(auctionId, new Auction(auctionId, description, owner, end));
        timer.schedule(new AuctionTimerTask(this, auctionId), end);

        return auctionId;
    }

    public synchronized void bid(int auctionId, User bidder, int amount) {
        if (!auctions.containsKey(auctionId)) {
            System.err.println("No such auction");
            return;
        }
        auctions.get(auctionId).bid(bidder, amount);
    }

    private synchronized void expire(int auctionId) {
        notifyListeners(new AuctionEvent(AuctionEvent.AUCTION_ENDED, auctionId));
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

    /**
     * Listeners are notified of all auction related events.
     */
    public interface EventListener {
        void onEvent(Event event);
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
