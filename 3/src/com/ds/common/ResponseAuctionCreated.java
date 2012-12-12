
package com.ds.common;

import java.util.StringTokenizer;

public class ResponseAuctionCreated extends Response {

    private static final long serialVersionUID = 2523937474952629824L;

    private final int id;

    public ResponseAuctionCreated(int id) {
        super(Rsp.AUCTION_CREATED);
        this.id = id;
    }

    protected ResponseAuctionCreated(StringTokenizer st) {
        super(Rsp.AUCTION_CREATED);

        if (st.countTokens() != 1) {
            throw new IllegalArgumentException();
        }

        this.id = Integer.parseInt(st.nextToken());
    }

    @Override
    public String toNetString() {
        return String.format("%s %d", super.toString(), id);
    }

    @Override
    public String toString() {
        return String.format("Created auction with id %d%n", id);
    }
}
