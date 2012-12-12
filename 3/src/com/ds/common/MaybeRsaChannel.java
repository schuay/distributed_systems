package com.ds.common;

import java.net.Socket;
import java.security.PrivateKey;
import java.io.IOException;

import javax.crypto.Cipher;

import com.ds.util.SecurityUtils;

public class MaybeRsaChannel extends TcpChannel {
    private final PrivateKey privateKey;

    public MaybeRsaChannel(Socket socket, PrivateKey privateKey) throws IOException {
        super(socket);
        this.privateKey = privateKey;
    }
    public void printf(String format, Object... args) {
        super.printf(format, args);
    }

    public String readLine() throws IOException {
        String msg = super.readLine();
        if (msg == null) {
            return null;
        }

        try {
            Cipher cipher = SecurityUtils.getCipher(
                    "RSA/NONE/OAEPWithSHA256AndMGF1Padding",
                    Cipher.DECRYPT_MODE,
                    privateKey,
                    null);
            byte[] dcrypt = cipher.doFinal(SecurityUtils.fromBase64(msg.getBytes()));
            return new String(dcrypt);
        } catch (Throwable t) {
            return msg;
        }
    }

    public void close() {
        super.close();
    }
}
