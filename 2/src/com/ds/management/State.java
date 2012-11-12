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
            case SUBSCRIBE: {
                String filter = args.get(0);
                String subscriptionID = data.getAnalSub().subscribe(filter);
                System.out.printf("Created subscription with ID %s for events using filter %s%n",
                        subscriptionID, filter);
                return this;
            }
            case UNSUBSCRIBE: {
                String subscriptionID = args.get(0);
                data.getAnalSub().unsubscribe(subscriptionID);
                System.out.printf("Subscription %s terminated%n", subscriptionID);
                return this;
            }
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