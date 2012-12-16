package com.ds.client;


public class Parcel {

    public enum Type {
        PARCEL_NETWORK,
        PARCEL_TERMINAL
    }

    private final Type type;
    private final String message;

    public Parcel(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

}
