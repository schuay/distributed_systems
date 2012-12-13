package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.loggers.Log;
import com.ds.util.SecurityUtils;

/**
 * RsaChannel encrypts all outgoing traffic with a public key,
 * and decrypts all incoming traffic with a private key.
 */
public class RsaChannel implements Channel {

    private final Channel channel;
    private final Cipher dcrypt; /**< Decryption cipher. */
    private final Cipher ecrypt; /**< Encryption cipher. */

    public RsaChannel(Channel channel, PublicKey publicKey, PrivateKey privateKey)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.channel = channel;
        dcrypt = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.DECRYPT_MODE, privateKey, null);
        ecrypt = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.ENCRYPT_MODE, publicKey, null);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        try {
            byte[] msg = ecrypt.doFinal(bytes);
            channel.write(msg);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), TcpChannel.CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        byte[] msg = channel.read();
        if (msg == null) {
            return null;
        }

        try {
            return dcrypt.doFinal(SecurityUtils.fromBase64(msg));
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public void close() {
        try {
            dcrypt.doFinal();
            ecrypt.doFinal();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }
}
