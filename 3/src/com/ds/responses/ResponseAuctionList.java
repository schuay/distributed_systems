
package com.ds.responses;

import com.ds.server.AuctionList;

public class ResponseAuctionList extends Response {

    private static final long serialVersionUID = -264808809971598170L;

    private final String auctionListString;

    public ResponseAuctionList(AuctionList auctionList) {
        super(Rsp.AUCTION_LIST);

        /* Sloppy escape.
         * Since we use '#' in place of a '\n' (for transmission as a single line
         * string), escape all '#'.
         */

        auctionListString = auctionList.toString()
                .replaceAll("#", "\\#")
                .replaceAll("\\n", "#");
    }

    public ResponseAuctionList(String auctionListString) {
        super(Rsp.AUCTION_LIST);

        /* Sloppy unescape. */

        this.auctionListString = auctionListString
                .replaceAll("\\\\#", "\0")
                .replaceAll("#", "\n")
                .replaceAll("\0", "#");
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
