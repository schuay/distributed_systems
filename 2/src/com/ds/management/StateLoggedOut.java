package com.ds.management;

import java.util.List;

import com.ds.management.ManagementMain.Data;


/**
 * Handles user input while the user is logged out.
 */
class StateLoggedOut extends State {

    public StateLoggedOut(Data data) {
        super(data);
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
            case LOGIN:
                /* TODO: Manage authentication. */
                return new StateLoggedIn(getData(), args.get(0));
            default:
                throw new IllegalArgumentException(String.format(
                        "Invalid command in state %s", StateLoggedOut.class.getName()));
        }
    }

    @Override
    public String getPrefix() {
        return "> ";
    }

}
