package com.ds.client;


public class StringParcel extends Parcel {

    private final String message;

    public StringParcel(Type type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
