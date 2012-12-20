package com.ds.client;

import com.ds.commands.CommandSignedBid;

public class TimestampResultParcel extends Parcel {

    private final CommandSignedBid command;

    public TimestampResultParcel(CommandSignedBid command) {
        super(Type.PARCEL_TIMESTAMP_RESULT);
        this.command = command;
    }

    public CommandSignedBid getCommand() {
        return command;
    }
}
