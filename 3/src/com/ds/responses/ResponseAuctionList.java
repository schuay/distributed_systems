
package com.ds.responses;

import com.ds.server.AuctionList;
import com.ds.util.Multiline;

public class ResponseAuctionList extends Response {

    private static final long serialVersionUID = -264808809971598170L;

    private final String auctionListString;

    public ResponseAuctionList(AuctionList auctionList) {
        super(Rsp.AUCTION_LIST);
        this.auctionListString = auctionList.toString();
    }

    public ResponseAuctionList(String auctionListString) {
        super(Rsp.AUCTION_LIST);
        this.auctionListString = Multiline.decode(auctionListString);
    }

    @Override
    public String toNetString() {
        return String.format("%s %s", super.toNetString(), Multiline.encode(auctionListString));
    }

    @Override
    public String toString() {
        return auctionListString;
    }
}
