package com.ds.common;

import com.ds.server.AuctionList;

public class AuctionListResponse extends Response {

	private static final long serialVersionUID = -264808809971598170L;
	private final String auctionListString;

	public AuctionListResponse(AuctionList auctionList) {
		super(Rsp.AUCTION_LIST);

		auctionListString = auctionList.toString();
	}

	@Override
	public String toString() {
		return auctionListString;
	}
}
