package com.ds.event;

public class AuctionEvent extends Event {

    private static final long serialVersionUID = 1L;
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
