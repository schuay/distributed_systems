package com.ds.billing;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

import com.ds.util.UserProperties;

class Authentication {
    private static final int DIGEST_RADIX = 16;

    private final UserProperties up;
    private final MessageDigest md;

    Authentication() throws IOException, NoSuchAlgorithmException {
        up = new UserProperties();
        md = MessageDigest.getInstance("MD5");
    }

    boolean loginValid(String username, String password) {
        String pwDigest = up.getHash(username);
        if (pwDigest == null) {
            return false;
        }

        /* Turn the stored hash back into a number. */
        byte[] stored = DatatypeConverter.parseHexBinary(pwDigest);
        byte[] received;

        synchronized (this) {
            md.reset();
            received = md.digest(password.getBytes());
        }

        return MessageDigest.isEqual(stored, received);
    }
}
