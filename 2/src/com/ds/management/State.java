package com.ds.management;

import java.util.List;


/**
 * The state is responsible for handling user input events.
 */
abstract class State {

    /**
     * Returns the next state to set, or null is not responsible for the
     * passed command (for example, if it should be handled by a subclass instead).
     */
    State processCommand(CommandMatcher.Type type, List<String> args) {
        switch (type) {
            default:
                return null;
        }
    }

    public abstract String getPrefix();
}