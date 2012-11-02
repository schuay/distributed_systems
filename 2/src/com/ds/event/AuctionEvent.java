package com.ds.event;

public class AuctionEvent extends Event {

    public static final String AUCTION_STARTED = "AUCTION_STARTED";
    public static final String AUCTION_ENDED = "AUCTION_ENDED";

    private final long auctionID;

    public AuctionEvent(String type, long auctionID) {
        super(type);
        this.auctionID = auctionID;
    }

    public long getAuctionID() {
        return auctionID;
    }

    @Override
    public String toString() {
        return String.format("%s: Auction ID = %d", getType(), getAuctionID());
    }
}
