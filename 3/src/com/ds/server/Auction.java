
package com.ds.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
    private final List<Bid> bids = new LinkedList<Bid>();
    private final List<EventListener> listeners = new ArrayList<EventListener>();
    private final Map<String, Map<Integer, GroupBid> > groupBids =
            new HashMap<String, Map<Integer, GroupBid> >();

    public Auction(int id, String description, User owner, Date end) {
        this.id = id;
        this.description = description;
        this.owner = owner;
        this.end = end;

        /* Initial null bid. */

        bids.add(new Bid(0, User.NONE));
    }

    static class Bid {
        private final int amount;
        private final User user;
        private final long timestamp;

        public Bid(int amount, User user) {
            this(amount, user, System.currentTimeMillis());
        }

        public Bid(int amount, User user, long timestamp) {
            this.amount = amount;
            this.user = user;
            this.timestamp = timestamp;
        }

        public int getAmount() {
            return amount;
        }

        public User getUser() {
            return user;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    static class GroupBid {
        private final List<GroupBidListener> listeners =
                new ArrayList<GroupBidListener>(GroupBidMonitor.GROUP_SIZE);
        private final User bidder;
        private final int amount;
        private int numBidders;
        private boolean rejected = false;
        private final Auction auction;

        private GroupBid(Auction auction, User bidder, int amount) {
            this.auction = auction;
            this.bidder = bidder;
            this.amount = amount;
            this.numBidders = 1;
        }

        int getNumBidders() {
            return numBidders;
        }

        void confirm(GroupBidListener listener) {
            listeners.add(listener);
            numBidders++;
            if (confirmed()) {
                notifyListenersConfirmed();
            }
        }

        void reject() {
            rejected = true;
            notifyListenersRejected();
        }

        boolean confirmed() {
            return (!rejected && numBidders >= GroupBidMonitor.GROUP_SIZE);
        }

        User getBidder() {
            return bidder;
        }

        int getAmount() {
            return amount;
        }

        Auction getAuction() {
            return auction;
        }

        private void notifyListenersConfirmed() {
            for (GroupBidListener listener : listeners) {
                listener.onConfirmed();
            }
        }

        private void notifyListenersRejected() {
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

            return groupBid.bidder == bidder && groupBid.amount == amount;
        }
    }

    private Bid highestBid() {
        if (bids.isEmpty()) {
            return null;
        }

        return bids.get(bids.size() - 1);
    }

    public boolean createGroupBid(User bidder, int amount) {
        String bidderName = bidder.getName();

        Map<Integer, GroupBid> bMap = groupBids.get(bidderName);
        if (bMap == null) {
            bMap = new HashMap<Integer, GroupBid>();
            groupBids.put(bidderName, bMap);
        }

        GroupBid groupBid = new GroupBid(this, bidder, amount);

        if (bMap.containsValue(groupBid)) {
            return false;
        }

        bMap.put(amount, groupBid);

        return true;
    }

    GroupBid getGroupBid(String bidder, int amount) {
        Map<Integer, GroupBid> bMap = groupBids.get(bidder);
        if (bMap == null) {
            return null;
        }

        return bMap.get(amount);
    }

    void removeGroupBid(String bidder, int amount) {
        Map<Integer, GroupBid> bMap = groupBids.get(bidder);

        bMap.remove(amount);
        if (bMap.isEmpty()) {
            groupBids.remove(bidder);
        }
    }

    public void bid(User bidder, int amount) {
        if (amount <= highestBid().getAmount()) {
            return;
        }

        /* I'm assuming that the overbid event should include the previous high bidder
         * and the new price. */
        notifyListeners(new BidEvent(BidEvent.BID_OVERBID, highestBid().getUser().getName(), id, amount));
        notifyListeners(new BidEvent(BidEvent.BID_PLACED, bidder.getName(), id, amount));

        bids.add(new Bid(amount, bidder));
    }

    public void end() {
        for (Map<Integer, GroupBid> m : groupBids.values()) {
            for (GroupBid b : m.values()) {
                b.reject();
            }
        }

        if (highestBid().getUser() != User.NONE) {
            notifyListeners(new BidEvent(BidEvent.BID_WON, highestBid().getUser().getName(),
                    id, highestBid().getAmount()));
        }
    }

    @Override
    public String toString() {
        return String.format("%d. '%s' %s %s %d %s", id, description, owner.getName(), end,
                highestBid().getAmount(), highestBid().getUser().getName());
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
