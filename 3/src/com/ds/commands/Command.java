
package com.ds.commands;

import java.io.Serializable;
import java.util.StringTokenizer;

public class Command implements Serializable {

    private static final long serialVersionUID = 4698051331006702570L;

    public enum Cmd {
        LOGIN, LOGOUT, LIST, CREATE, BID, END, CHALLENGE, PASSPHRASE
    }

    private final Cmd type;
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
            } else if (token.equals("!pass")) {
                return new CommandPassphrase(token, st);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        throw new IllegalArgumentException();
    }

    public Command(String cmdStr, Cmd type) {
        this.cmdStr = cmdStr;
        this.type = type;
    }

    public Cmd getType() {
        return type;
    }

    @Override
    public String toString() {
        return cmdStr;
    }
}
