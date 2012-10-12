package com.ds.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionList {

	private final ConcurrentHashMap<Integer, Auction> auctions = new ConcurrentHashMap<>();
	private int id = 0;

	public synchronized void add(String description, String owner, Date end) {
		int auctionId = id++;
		auctions.put(auctionId, new Auction(auctionId, description, owner, end));
	}

	public List<Auction> getAuctions() {
		return Collections.unmodifiableList(new ArrayList<Auction>(auctions.values()));
	}
}
