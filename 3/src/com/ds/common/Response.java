
package com.ds.common;

import java.io.Serializable;
import java.util.StringTokenizer;

public class Response implements Serializable {

    private static final long serialVersionUID = 7978041566542954342L;

    private final Rsp response;

    public Rsp getResponse() {
        return response;
    }

    public enum Rsp {
        OK, ERROR, AUCTION_LIST, AUCTION_CREATED, BID, CHALLENGE
    }

    public static Response parse(String line) {
        StringTokenizer st = new StringTokenizer(line);
        if (!st.hasMoreTokens()) {
            throw new IllegalArgumentException();
        }

        try {
            String token = st.nextToken().toLowerCase();
            if (token.equals("!ok")) {
                return new Response(Rsp.OK);
            } else if (token.equals("!error")) {
                return new Response(Rsp.ERROR);
            } else if (token.equals("!auction_list")) {
                return new ResponseAuctionList(line.substring(token.length()).trim());
            } else if (token.equals("!auction_created")) {
                return new ResponseAuctionCreated(st);
            } else if (token.equals("!bid")) {
                return new Response(Rsp.BID);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        throw new IllegalArgumentException();
    }

    public Response(Rsp response) {
        this.response = response;
    }

    public String toNetString() {
        return String.format("!%s", response.toString().toLowerCase());
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
