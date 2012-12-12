package com.ds.channels;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.util.SecurityUtils;

/**
 * MaybeRsaChannel is identical to TcpChannel, except that it can unwrap incoming messages
 * encrypted with a public key (to which we have the matching private key).
 * 
 * The expected message format is either plain strings, or rsa encrypted messages encoded as
 * base64.
 */
public class MaybeRsaChannel extends TcpChannel {

    private final Cipher cipher;

    public MaybeRsaChannel(Socket socket, PrivateKey privateKey)
            throws IOException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException {

        super(socket);
        cipher = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.DECRYPT_MODE,
                privateKey, null);
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        byte[] msg = super.read();
        if (msg == null) {
            return null;
        }

        try {
            byte[] dcrypt = cipher.doFinal(SecurityUtils.fromBase64(msg));
            return dcrypt;
        } catch (Throwable t) {
            return msg;
        }
    }
}
