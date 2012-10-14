package com.ds.server;

import java.util.Date;

public class Auction {
	private final int id;
	private final String description;
	private final User owner;
	private final Date end;
	private final int highestBid = 0;
	private final String highestBidder = "none";

	public Auction(int id, String description, User owner, Date end) {
		this.id = id;
		this.description = description;
		this.owner = owner;
		this.end = end;
	}

	@Override
	public String toString() {
		return String.format("%d. '%s' %s %s %d %s",
				id, description, owner.getName(), end, highestBid, highestBidder);
	}
}
