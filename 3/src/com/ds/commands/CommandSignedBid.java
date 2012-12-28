
package com.ds.commands;


public class CommandSignedBid extends Command {

    private static final long serialVersionUID = 3662230444450063848L;

    private final int auctionId;
    private final int amount;
    private final String user1;
    private final long timestamp1;
    private final String signature1;
    private final String user2;
    private final long timestamp2;
    private final String signature2;

    public int getAuctionId() {
        return auctionId;
    }

    public int getAmount() {
        return amount;
    }

    public String getUser1() {
        return user1;
    }

    public long getTimestamp1() {
        return timestamp1;
    }

    public String getSignature1() {
        return signature1;
    }

    public String getUser2() {
        return user2;
    }

    public long getTimestamp2() {
        return timestamp2;
    }

    public String getSignature2() {
        return signature2;
    }

    public CommandSignedBid(String cmdStr, int auctionId, int amount,
            String user1, long timestamp1, String signature1,
            String user2, long timestamp2, String signature2) {
        super(cmdStr, Cmd.SIGNEDBID);
        this.auctionId = auctionId;
        this.amount = amount;
        this.user1 = user1;
        this.timestamp1 = timestamp1;
        this.signature1 = signature1;
        this.user2 = user2;
        this.timestamp2 = timestamp2;
        this.signature2 = signature2;
    }

    /* This is not an interactive command, and thus does not need a parsing
     * constructor. */

    @Override
    public String toString() {
        return String.format("%s %d %d %s:%s:%s %s:%s:%s",
                super.toString(), auctionId, amount,
                user1, timestamp1, signature1,
                user2, timestamp2, signature2);
    }
}
