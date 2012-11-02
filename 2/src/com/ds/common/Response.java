
package com.ds.common;

import java.io.Serializable;

public class Response implements Serializable {

    private static final long serialVersionUID = 7978041566542954342L;

    private final Rsp response;

    public Rsp getResponse() {
        return response;
    }

    public enum Rsp {
        OK, ERROR, AUCTION_LIST, AUCTION_CREATED, BID
    }

    public Response(Rsp response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
