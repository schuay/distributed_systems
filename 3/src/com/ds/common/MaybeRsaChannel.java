package com.ds.common;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.util.SecurityUtils;

public class MaybeRsaChannel extends TcpChannel {

    private final Cipher cipher;

    public MaybeRsaChannel(Socket socket, PrivateKey privateKey)
            throws IOException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {
        super(socket);
        cipher = SecurityUtils.getCipher(
                "RSA/NONE/OAEPWithSHA256AndMGF1Padding",
                Cipher.DECRYPT_MODE,
                privateKey,
                null);
    }

    @Override
    public void printf(String format, Object... args) {
        super.printf(format, args);
    }

    @Override
    public String readLine() throws IOException {
        String msg = super.readLine();
        if (msg == null) {
            return null;
        }

        try {
            byte[] dcrypt = cipher.doFinal(SecurityUtils.fromBase64(msg.getBytes()));
            return new String(dcrypt);
        } catch (Throwable t) {
            return msg;
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
