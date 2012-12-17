package com.ds.commands;

import java.util.StringTokenizer;

public class CommandConfirm extends Command {

    private static final long serialVersionUID = 1L;

    private final int auctionId;

    public int getAuctionId() {
        return auctionId;
    }

    private final int amount;

    public int getAmount() {
        return amount;
    }

    private final String user;

    public String getUser() {
        return user;
    }

    public CommandConfirm(String cmdStr, int auctionId, int amount, String user) {
        super(cmdStr, Cmd.CONFIRM);
        this.auctionId = auctionId;
        this.amount = amount;
        this.user = user;
    }

    protected CommandConfirm(String cmdStr, StringTokenizer st) {
        super(cmdStr, Cmd.CONFIRM);

        if (st.countTokens() < 3) {
            throw new IllegalArgumentException();
        }

        this.auctionId = Integer.parseInt(st.nextToken());
        this.amount = Integer.parseInt(st.nextToken());
        this.user = st.nextToken();
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %s", super.toString(), auctionId, amount, user);
    }

}
