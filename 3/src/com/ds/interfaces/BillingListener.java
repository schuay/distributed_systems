package com.ds.interfaces;

/**
 * Listeners are notified once an auction is billed.
 */
public interface BillingListener {
    void billAuction(String user, long auctionID, double price);
}
