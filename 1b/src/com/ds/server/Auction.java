package com.ds.server;

import java.util.Date;

public class Auction {
	private final int id;
	private final String description;
	private final User owner;
	private final Date end;
	private int highestBid = 0;
	private User highestBidder = new User("none");

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
		highestBid = amount;
		highestBidder = bidder;
	}

	@Override
	public String toString() {
		return String.format("%d. '%s' %s %s %d %s",
				id, description, owner.getName(), end,
				highestBid, highestBidder.getName());
	}
}