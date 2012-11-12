package com.ds.management;

import java.util.List;

import com.ds.management.ManagementMain.Data;


/**
 * Handles user input while the user is logged in.
 */
class StateLoggedIn extends State {

    private final String user;

    public StateLoggedIn(Data data, String user) {
        super(data);
        this.user = user;
    }

    @Override
    public State processCommand(CommandMatcher.Type type, List<String> args) {
        State next = super.processCommand(type, args);
        if (next != null) {
            return next;
        }

        switch (type) {
            case LOGOUT:
                return new StateLoggedOut(getData());
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
