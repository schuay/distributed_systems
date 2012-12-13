
package com.ds.responses;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import com.ds.util.SecurityUtils;


public class ResponseChallenge extends Response {

    private static final long serialVersionUID = 2523937474952629824L;

    private final byte[] clientChallenge;
    private final byte[] serverChallenge;
    private final byte[] iv;
    private final SecretKey secretKey;

    public ResponseChallenge(byte[] clientChallenge) throws NoSuchAlgorithmException {
        super(Rsp.CHALLENGE);
        this.clientChallenge = clientChallenge;
        this.serverChallenge = SecurityUtils.getSecureRandom();
        this.secretKey = SecurityUtils.generateSecretKey();
        this.iv = SecurityUtils.getSecureRandom();
    }

    public byte[] getClientChallenge() {
        return clientChallenge;
    }

    public byte[] getServerChallenge() {
        return serverChallenge;
    }

    public byte[] getIv() {
        return iv;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Override
    public String toNetString() {
        return String.format("!ok %s %s %s %s",
                SecurityUtils.toBase64(clientChallenge),
                SecurityUtils.toBase64(serverChallenge),
                SecurityUtils.toBase64(secretKey.getEncoded()),
                SecurityUtils.toBase64(iv));
    }
}