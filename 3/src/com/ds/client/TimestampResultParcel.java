package com.ds.client;

import com.ds.commands.CommandSignedBid;

public class TimestampResultParcel extends Parcel {

    private final CommandSignedBid command;
    private final String user;

    public TimestampResultParcel(String user, CommandSignedBid command) {
        super(Type.PARCEL_TIMESTAMP_RESULT);
        this.command = command;
        this.user = user;
    }

    public CommandSignedBid getCommand() {
        return command;
    }

    public String getUser() {
        return user;
    }
}
