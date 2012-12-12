
package com.ds.common;

import java.util.StringTokenizer;

import com.ds.util.SecurityUtils;

public class CommandLogin extends Command {

    private static final long serialVersionUID = 8266776473396356465L;

    private final String user;
    private final byte[] challenge;

    public CommandLogin(String cmdStr, String user, byte[] challenge) {
        super(cmdStr, Cmd.LOGIN);
        this.user = user;
        this.challenge = challenge;
    }

    protected CommandLogin(String cmdStr, StringTokenizer st) {
        super(cmdStr, Cmd.LOGIN);

        if (st.countTokens() < 1) {
            throw new IllegalArgumentException();
        }

        this.user = st.nextToken();
        this.challenge = SecurityUtils.getSecureRandom();
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        byte[] base64Challenge = SecurityUtils.toBase64(challenge);
        return String.format("%s %s %s",
                super.toString(),
                user,
                new String(base64Challenge));
    }
}
