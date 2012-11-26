package com.ds.management;

import java.rmi.RemoteException;
import java.util.List;

import com.ds.billing.BillingServerSecure;
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
                String username = args.get(0);
                String password = args.get(1);

                BillingServerSecure billing = null;
                try {
                    billing = getData().getBillSub().getBillingServer()
                            .login(username, password);
                } catch (RemoteException e) {
                    System.out.println(e.getCause().getLocalizedMessage());
                    return this;
                }

                System.out.printf("%s successfully logged in%n", username);
                return new StateLoggedIn(getData(), args.get(0), billing);
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
