package com.ds.client;

import com.ds.commands.CommandBid;

/**
 * Encapsulates a request for a timestamp peer retrieval.
 */
class TimeRequest {

    private final CommandBid command;
    private final User user1;
    private final User user2;

    public TimeRequest(CommandBid command, User user1, User user2) {
        this.command = command;
        this.user1 = user1;
        this.user2 = user2;
    }

    public CommandBid getCommand() {
        return command;
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }

}
