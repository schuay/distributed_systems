package com.ds.client;

import java.security.PrivateKey;

import com.ds.commands.CommandBid;

/**
 * Encapsulates a request for a timestamp peer retrieval.
 */
interface P2PTask {

    enum Type {
        GET_TIMESTAMP,
        LOG_IN,
        LOG_OUT
    }

    Type getType();

}

class P2PGetTimestampTask implements P2PTask {

    private final CommandBid command;

    public P2PGetTimestampTask(CommandBid command) {
        this.command = command;
    }

    public CommandBid getCommand() {
        return command;
    }

    @Override
    public Type getType() {
        return Type.GET_TIMESTAMP;
    }
}

class P2PLoginTask implements P2PTask {

    private final String user;
    private final PrivateKey key;

    public P2PLoginTask(String user, PrivateKey key) {
        this.user = user;
        this.key = key;
    }

    public String getUser() {
        return user;
    }

    public PrivateKey getKey() {
        return key;
    }

    @Override
    public Type getType() {
        return Type.LOG_IN;
    }

}

class P2PLogoutTask implements P2PTask {

    @Override
    public Type getType() {
        return Type.LOG_OUT;
    }

}