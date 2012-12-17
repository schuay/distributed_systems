
package com.ds.commands;

import java.util.StringTokenizer;

import com.ds.util.SecurityUtils;

public class CommandLogin extends Command {

    private static final long serialVersionUID = 8266776473396356465L;

    private final String user;
    private int port;
    private final byte[] challenge;

    public CommandLogin(String cmdStr, String user, int port, byte[] challenge) {
        super(cmdStr, Cmd.LOGIN);
        this.user = user;
        this.port = port;
        this.challenge = challenge;
    }

    protected CommandLogin(String cmdStr, StringTokenizer st) {
        super(cmdStr, Cmd.LOGIN);

        if (st.countTokens() < 1) {
            throw new IllegalArgumentException();
        }

        this.user = st.nextToken();
        this.challenge = SecurityUtils.getSecureRandom(SecurityUtils.CHALLENGE_BYTES);

        /* Unfortunately, we can't set the port here. */
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public String getUser() {
        return user;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        byte[] base64Challenge = SecurityUtils.toBase64(challenge);
        return String.format("%s %s %d %s",
                super.toString(),
                user,
                port,
                new String(base64Challenge));
    }
}
