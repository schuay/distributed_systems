package com.ds.commands;

import java.util.StringTokenizer;

public class CommandPassphrase extends Command {

    private static final long serialVersionUID = 1L;

    private final String passphrase;

    protected CommandPassphrase(String cmdStr, StringTokenizer st) {
        super(cmdStr, Cmd.PASSPHRASE);

        /* Limitation: Passphrase may not include whitespace. */

        if (st.countTokens() != 1) {
            throw new IllegalArgumentException();
        }

        this.passphrase = st.nextToken();
    }

    public String getPassphrase() {
        return passphrase;
    }

}
