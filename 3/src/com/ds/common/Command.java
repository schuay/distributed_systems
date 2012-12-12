
package com.ds.common;

import java.io.Serializable;
import java.util.StringTokenizer;

public class Command implements Serializable {

    private static final long serialVersionUID = 4698051331006702570L;

    public enum Cmd {
        LOGIN, LOGOUT, LIST, CREATE, BID, END, CHALLENGE
    }

    private final Cmd id;
    private final String cmdStr;

    public static Command parse(String line) {
        StringTokenizer st = new StringTokenizer(line);
        if (!st.hasMoreTokens()) {
            throw new IllegalArgumentException();
        }

        try {
            String token = st.nextToken().toLowerCase();
            if (token.equals("!logout")) {
                return new Command(token, Cmd.LOGOUT);
            } else if (token.equals("!list")) {
                return new Command(token, Cmd.LIST);
            } else if (token.equals("!end")) {
                return new Command(token, Cmd.END);
            } else if (token.equals("!login")) {
                return new CommandLogin(token, st);
            } else if (token.equals("!create")) {
                return new CommandCreate(token, st);
            } else if (token.equals("!bid")) {
                return new CommandBid(token, st);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        throw new IllegalArgumentException();
    }

    public Command(String cmdStr, Cmd id) {
        this.cmdStr = cmdStr;
        this.id = id;
    }

    public Cmd getId() {
        return id;
    }

    @Override
    public String toString() {
        return cmdStr;
    }
}
