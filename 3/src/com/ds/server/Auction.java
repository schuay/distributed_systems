
package com.ds.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ds.event.BidEvent;
import com.ds.event.Event;
import com.ds.loggers.EventListener;
import com.ds.server.UserList.User;

public class Auction {
    private final int id;
    private final String description;
    private final User owner;
    private final Date end;
    private int highestBid = 0;
    private User highestBidder = User.NONE;
    private final List<EventListener> listeners = new ArrayList<EventListener>();
    private final Map<String, Map<Integer, GroupBid> > groupBids =
            new HashMap<String, Map<Integer, GroupBid> >();

    public Auction(int id, String description, User owner, Date end) {
        this.id = id;
        this.description = description;
        this.owner = owner;
        this.end = end;
    }

    private class GroupBid {
        private static final int GROUP_SIZE = 3;

        private final List<GroupBidListener> listeners =
                new ArrayList<GroupBidListener>(GROUP_SIZE);
        private final String initBidder;
        private final int amount;
        private int numBidders;

        public GroupBid(String initBidder, int amount) {
            this.initBidder = initBidder;
            this.amount = amount;
            this.numBidders = 1;
        }

        public void confirm(GroupBidListener listener) {
            listeners.add(listener);
            numBidders++;
        }

        public boolean confirmed() {
            return numBidders >= GROUP_SIZE;
        }

        public void notifyListenersConfirmed() {
            for (GroupBidListener listener : listeners) {
                listener.onConfirmed();
            }
        }

        public void notifyListenersRejected() {
            for (GroupBidListener listener : listeners) {
                listener.onRejected();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof GroupBid)) {
                return false;
            }

            GroupBid groupBid = (GroupBid) o;

            return groupBid.initBidder == initBidder && groupBid.amount == amount;
        }
    }

    public boolean groupBid(User initBidder, int amount) {
        String bidderName = initBidder.getName();

        Map<Integer, GroupBid> bMap = groupBids.get(bidderName);
        if (bMap == null) {
            bMap = new HashMap<Integer, GroupBid>();
            groupBids.put(bidderName, bMap);
        }

        GroupBid groupBid = new GroupBid(bidderName, amount);

        if (bMap.containsValue(groupBid)) {
            return false;
        }

        bMap.put(amount, groupBid);

        return true;
    }

    public boolean confirmGroupBid(User initBidder, int amount, GroupBidListener listener) {
        String bidderName = initBidder.getName();

        Map<Integer, GroupBid> bMap = groupBids.get(bidderName);
        if (bMap == null) {
            return false;
        }

        GroupBid groupBid = bMap.get(amount);

        if (groupBid == null) {
            return false;
        }

        groupBid.confirm(listener);

        if (groupBid.confirmed()) {
            groupBid.notifyListenersConfirmed();
            bid(initBidder, amount);

            bMap.remove(amount);
            if (bMap.isEmpty()) {
                groupBids.remove(bidderName);
            }
        }

        return true;
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
        if (highestBidder != User.NONE) {
            notifyListeners(new BidEvent(BidEvent.BID_WON, highestBidder.getName(), id, highestBid));
        }
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
