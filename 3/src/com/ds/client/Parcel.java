package com.ds.client;


public class Parcel {

    public enum Type {
        PARCEL_CONNECTION_LOST,
        PARCEL_CONNECTION_ESTABLISHED,
        PARCEL_NETWORK,
        PARCEL_TERMINAL,
        PARCEL_TIMESTAMP_RESULT
    }

    private final Type type;

    public Parcel(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

}
