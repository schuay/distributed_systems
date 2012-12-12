
package com.ds.common;


public class CommandChallenge extends Command {

    private static final long serialVersionUID = 8266776473396356465L;

    private final byte[] challenge;

    public CommandChallenge(byte[] challenge) {
        super(new String(challenge), Cmd.CHALLENGE);
        this.challenge = challenge;
    }

    public byte[] getChallenge() {
        return challenge;
    }
}
