package com.ds.event;

public class BidEvent extends Event {

    private static final long serialVersionUID = 1L;
    public static final String BID_PLACED = "BID_PLACED";
    public static final String BID_OVERBID = "BID_OVERBID";
    public static final String BID_WON = "BID_WON";

    private final String user;
    private final long auctionID;
    private final double price;

    public BidEvent(String type, String user, long auctionID, double price) {
        super(type);

        this.user = user;
        this.auctionID = auctionID;
        this.price = price;
    }

    public String getUser() {
        return user;
    }

    public long getAuctionID() {
        return auctionID;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("%s: Auction ID = %d, User = %s, Price = %f",
                getType(), getAuctionID(), getUser(), getPrice());
    }
}
