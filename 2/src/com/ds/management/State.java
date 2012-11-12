package com.ds.management;

import java.util.List;

import com.ds.management.ManagementMain.Data;


/**
 * The state is responsible for handling user input events.
 */
abstract class State {

    private final Data data;

    public State(Data data) {
        this.data = data;
    }

    /**
     * Returns the next state to set, or null is not responsible for the
     * passed command (for example, if it should be handled by a subclass instead).
     */
    State processCommand(CommandMatcher.Type type, List<String> args) {
        switch (type) {
            case SUBSCRIBE:
                return this;
            case UNSUBSCRIBE:
                return this;
            case AUTO:
                return this;
            case HIDE:
                return this;
            case PRINT:
                return this;
            default:
                return null;
        }
    }

    public abstract String getPrefix();

    protected Data getData() {
        return data;
    }
}