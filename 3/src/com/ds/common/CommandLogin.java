
package com.ds.common;

import java.util.StringTokenizer;

public class CommandLogin extends Command {

    private static final long serialVersionUID = 8266776473396356465L;

    private final String user;
    private byte[] challenge;

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
    }

    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        if (challenge != null) {
            return String.format("%s %s %s", super.toString(), user, challenge);
        }

        return String.format("%s %s", super.toString(), user);
    }
}
