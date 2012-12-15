
package com.ds.responses;

import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import javax.crypto.SecretKey;

import com.ds.util.SecurityUtils;


public class ResponseOk extends Response {

    private static final long serialVersionUID = 2523937474952629824L;

    private final byte[] clientChallenge;
    private final byte[] serverChallenge;
    private final byte[] iv;
    private final SecretKey secretKey;

    public ResponseOk(byte[] clientChallenge) throws NoSuchAlgorithmException {
        super(Rsp.OK);
        this.clientChallenge = clientChallenge;
        this.serverChallenge = SecurityUtils.getSecureRandom(SecurityUtils.CHALLENGE_BYTES);
        this.secretKey = SecurityUtils.generateSecretKey();
        this.iv = SecurityUtils.getSecureRandom(SecurityUtils.IV_BYTES);
    }

    protected ResponseOk(StringTokenizer st) {
        super(Rsp.OK);

        if (st.countTokens() != 4) {
            throw new IllegalArgumentException();
        }

        this.clientChallenge = SecurityUtils.fromBase64(st.nextToken().getBytes());
        this.serverChallenge = SecurityUtils.fromBase64(st.nextToken().getBytes());
        this.secretKey = SecurityUtils.readSecretKey(
                SecurityUtils.fromBase64(st.nextToken().getBytes()),
                SecurityUtils.AES);
        this.iv = SecurityUtils.fromBase64(st.nextToken().getBytes());
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
        return String.format("%s %s %s %s %s",
                super.toNetString(),
                new String(SecurityUtils.toBase64(clientChallenge)),
                new String(SecurityUtils.toBase64(serverChallenge)),
                new String(SecurityUtils.toBase64(secretKey.getEncoded())),
                new String(SecurityUtils.toBase64(iv)));
    }
}
