
package com.ds.commands;

import java.io.Serializable;
import java.util.StringTokenizer;

public class Command implements Serializable {

    private static final long serialVersionUID = 4698051331006702570L;

    public enum Cmd {
        BID,
        CHALLENGE,
        CONFIRM,
        CREATE,
        END,
        GETCLIENTLIST,
        GROUPBID,
        LIST,
        LOGIN,
        LOGOUT,
        PASSPHRASE,
        RETRY,
        SIGNEDBID
    }

    private final Cmd type;
    private final String cmdStr;

    /**
     * Called by the client to parse user input into commands.
     * The server does *not* use this method, and constructs commands
     * manually.
     */
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
            } else if (token.equals("!groupbid")) {
                return new CommandGroupBid(token, st);
            } else if (token.equals("!confirm")) {
                return new CommandConfirm(token, st);
            } else if (token.equals("!pass")) {
                return new CommandPassphrase(token, st);
            } else if (token.equals("!getclientlist")) {
                return new Command(token, Cmd.GETCLIENTLIST);
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
