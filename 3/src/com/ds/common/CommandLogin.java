
package com.ds.common;

import java.util.StringTokenizer;

public class CommandLogin extends Command {

    private static final long serialVersionUID = 8266776473396356465L;

    private final String user;

    protected CommandLogin(StringTokenizer st) {
        super(Cmd.LOGIN);

        if (st.countTokens() < 1) {
            throw new IllegalArgumentException();
        }

        this.user = st.nextToken();
    }

    public String getUser() {
        return user;
    }
}
