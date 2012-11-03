
package com.ds.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ds.event.BidEvent;
import com.ds.event.Event;
import com.ds.interfaces.EventListener;
import com.ds.server.UserList.User;

public class Auction {

    private final int id;
    private final String description;
    private final User owner;
    private final Date end;
    private int highestBid = 0;
    private User highestBidder = User.NONE;
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    public Auction(int id, String description, User owner, Date end) {
        this.id = id;
        this.description = description;
        this.owner = owner;
        this.end = end;
    }

    public void bid(User bidder, int amount) {
        if (amount <= highestBid) {
            return;
        }

        /* I'm assuming that the overbid event should include the previous high bidder
         * and the new price. */
        notifyListeners(new BidEvent(BidEvent.BID_OVERBID, highestBidder.getName(), id, amount));
        notifyListeners(new BidEvent(BidEvent.BID_PLACED, bidder.getName(), id, amount));

        highestBid = amount;
        highestBidder = bidder;
    }

    public void end() {
        notifyListeners(new BidEvent(BidEvent.BID_WON, highestBidder.getName(), id, highestBid));
    }

    @Override
    public String toString() {
        return String.format("%d. '%s' %s %s %d %s", id, description, owner.getName(), end,
                highestBid, getHighestBidder().getName());
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public User getOwner() {
        return owner;
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
}
