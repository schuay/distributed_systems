
package com.ds.responses;

import java.io.Serializable;
import java.util.StringTokenizer;

public class Response implements Serializable {

    private static final long serialVersionUID = 7978041566542954342L;

    private final Rsp response;

    public Rsp getResponse() {
        return response;
    }

    public enum Rsp {
        ACK,
        AUCTION_CREATED,
        AUCTION_LIST,
        BID,
        CLIENT_LIST,
        CONFIRMED,
        NAK,
        OK,
        REJECTED
    }

    public static Response parse(String line) {
        StringTokenizer st = new StringTokenizer(line);
        if (!st.hasMoreTokens()) {
            throw new IllegalArgumentException();
        }

        try {
            String token = st.nextToken().toLowerCase();
            if (token.equals("!ack")) {
                return new Response(Rsp.ACK);
            } else if (token.equals("!nak")) {
                return new Response(Rsp.NAK);
            } else if (token.equals("!auction_list")) {
                return new ResponseAuctionList(line.substring(token.length()).trim());
            } else if (token.equals("!auction_created")) {
                return new ResponseAuctionCreated(st);
            } else if (token.equals("!bid")) {
                return new Response(Rsp.BID);
            } else if (token.equals("!confirmed")) {
                return new Response(Rsp.CONFIRMED);
            } else if (token.equals("!rejected")) {
                return new Response(Rsp.REJECTED);
            } else if (token.equals("!ok")) {
                return new ResponseOk(st);
            } else if (token.equals("!client_list")) {
                return new ResponseClientList(line.substring(token.length()).trim());
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
