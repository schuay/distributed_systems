package com.ds.billing;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

import com.ds.util.SimpleProperties;

class Authentication extends SimpleProperties {
    private static final int DIGEST_RADIX = 16;

    private MessageDigest md;

    Authentication() throws IOException, NoSuchAlgorithmException {
        super("user.properties");

        md = MessageDigest.getInstance("MD5");
    }

    boolean loginValid(String username, String password) {
        String pwDigest = getProperty(username);
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
