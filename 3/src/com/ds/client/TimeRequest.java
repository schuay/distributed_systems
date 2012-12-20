package com.ds.client;

import com.ds.commands.CommandBid;

/**
 * Encapsulates a request for a timestamp peer retrieval.
 */
class TimeRequest {

    private final CommandBid command;

    public TimeRequest(CommandBid command) {
        this.command = command;
    }

    public CommandBid getCommand() {
        return command;
    }

}
