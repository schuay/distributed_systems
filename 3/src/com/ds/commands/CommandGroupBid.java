package com.ds.commands;

import java.util.StringTokenizer;

public class CommandGroupBid extends Command {

    private static final long serialVersionUID = 1L;

    private final int auctionId;

    public int getAuctionId() {
        return auctionId;
    }

    private final int amount;

    public int getAmount() {
        return amount;
    }

    public CommandGroupBid(String cmdStr, int auctionId, int amount) {
        super(cmdStr, Cmd.GROUPBID);
        this.auctionId = auctionId;
        this.amount = amount;
    }

    protected CommandGroupBid(String cmdStr, StringTokenizer st) {
        super(cmdStr, Cmd.GROUPBID);

        if (st.countTokens() < 2) {
            throw new IllegalArgumentException();
        }

        this.auctionId = Integer.parseInt(st.nextToken());
        this.amount = Integer.parseInt(st.nextToken());
    }

    @Override
    public String toString() {
        return String.format("%s %d %d", super.toString(), auctionId, amount);
    }

}
