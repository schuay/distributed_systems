
package com.ds.common;

import java.util.StringTokenizer;

public class CommandBid extends Command {

    private static final long serialVersionUID = 3662230444450063848L;

    private final int auctionId;

    public int getAuctionId() {
        return auctionId;
    }

    private final int amount;

    public int getAmount() {
        return amount;
    }

    protected CommandBid(StringTokenizer st) {
        super(Cmd.BID);

        if (st.countTokens() < 2) {
            throw new IllegalArgumentException();
        }

        this.auctionId = Integer.parseInt(st.nextToken());
        this.amount = Integer.parseInt(st.nextToken());
    }
}
