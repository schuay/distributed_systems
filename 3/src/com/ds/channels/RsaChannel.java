package com.ds.channels;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.util.SecurityUtils;

/**
 * RsaChannel encrypts all outgoing traffic with a public key,
 * and decrypts all incoming traffic with a private key.
 */
public class RsaChannel extends Channel {

    private final Channel channel;
    private final Cipher dcrypt; /**< Decryption cipher. */
    private final Cipher ecrypt; /**< Encryption cipher. */

    public RsaChannel(Channel channel, PublicKey publicKey, PrivateKey privateKey)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException {
        this.channel = channel;
        dcrypt = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.DECRYPT_MODE, privateKey, null);
        ecrypt = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.ENCRYPT_MODE, publicKey, null);
    }

    @Override
    public byte[] encode(byte[] in) throws IOException {
        try {
            byte[] b = ecrypt.doFinal(in);
            return channel.encode(b);
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public byte[] decode(byte[] in) throws IOException {
        try {
            byte[] b = channel.decode(in);
            flags = channel.getFlags() | FLAG_ENCRYPTED;
            return dcrypt.doFinal(b);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }
}
