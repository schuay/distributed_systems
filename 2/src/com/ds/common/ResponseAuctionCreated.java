package com.ds.common;


public class ResponseAuctionCreated extends Response {

	private static final long serialVersionUID = 2523937474952629824L;
	private final int id;

	public ResponseAuctionCreated(int id) {
		super(Rsp.AUCTION_CREATED);
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("Created auction with id %d%n", id);
	}
}
