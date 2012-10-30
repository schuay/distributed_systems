package com.ds.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionList {

    private final ConcurrentHashMap<Integer, Auction> auctions = new ConcurrentHashMap<Integer, Auction>();
    private final Timer timer = new Timer();
    private int id = 0;

    public synchronized int add(String description, User owner, Date end) {
        int auctionId = id++;

        auctions.put(auctionId, new Auction(auctionId, description, owner, end));
        timer.schedule(new AuctionTimerTask(this, auctionId), end);

        return auctionId;
    }

    public synchronized void bid(int id, User bidder, int amount) {
        if (!auctions.containsKey(id)) {
            System.err.println("No such auction");
            return;
        }
        auctions.get(id).bid(bidder, amount);
    }

    private synchronized void expire(int id) {
        Auction auction = auctions.get(id);
        auction.getHighestBidder().postNotification("The auction has ended. You won!");
        auction.getOwner().postNotification("The auction has ended.");
        auctions.remove(id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Auction auction : auctions.values()) {
            sb.append(String.format("%s%n", auction.toString()));
        }
        return sb.toString();
    }

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

    public void cancelTimers() {
        timer.cancel();
    }
}
