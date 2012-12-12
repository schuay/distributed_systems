
package com.ds.common;

import com.ds.server.AuctionList;

public class ResponseAuctionList extends Response {

    private static final long serialVersionUID = -264808809971598170L;

    private final String auctionListString;

    public ResponseAuctionList(AuctionList auctionList) {
        super(Rsp.AUCTION_LIST);

        auctionListString = auctionList.toString();
    }

    public ResponseAuctionList(String auctionListString) {
        super(Rsp.AUCTION_LIST);

        this.auctionListString = auctionListString;
    }

    @Override
    public String toNetString() {
        return String.format("%s %s", super.toNetString(), auctionListString);
    }

    @Override
    public String toString() {
        return auctionListString;
    }
}
