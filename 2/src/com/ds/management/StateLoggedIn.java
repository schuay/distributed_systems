package com.ds.management;

import java.util.List;


/**
 * Handles user input while the user is logged in.
 */
class StateLoggedIn extends State {

    private final String user;

    public StateLoggedIn(String user) {
        this.user = user;
    }

    @Override
    public State processCommand(
            CommandMatcher.Type type,
            List<String> args) {
        State next = super.processCommand(type, args);
        if (next != null) {
            return next;
        }

        switch (type) {
            case LOGOUT:
                return new StateLoggedOut();
            default:
                throw new IllegalArgumentException(String.format(
                        "Invalid command in state %s", StateLoggedIn.class.getName()));
        }
    }

    @Override
    public String getPrefix() {
        return String.format("%s> ", user);
    }

}
