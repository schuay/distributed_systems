package com.ds.server;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionList {

	private final ConcurrentHashMap<Integer, Auction> auctions = new ConcurrentHashMap<>();
	private int id = 0;

	public synchronized int add(String description, User owner, Date end) {
		int auctionId = id++;
		auctions.put(auctionId, new Auction(auctionId, description, owner, end));
		return auctionId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Auction auction : auctions.values()) {
			sb.append(String.format("%s%n", auction.toString()));
		}
		return sb.toString();
	}
}
