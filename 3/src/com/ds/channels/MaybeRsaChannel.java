package com.ds.channels;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.util.SecurityUtils;

/**
 * Encodes as a NOP, decodes by unwrap incoming messages
 * encrypted with a public key (to which we have the matching private key) if possible,
 * or as a NOP if not.
 * 
 * The expected message format is either plain strings, or rsa encrypted messages encoded as
 * base64.
 */
public class MaybeRsaChannel extends Channel {

    private final Cipher cipher;
    private final Channel channel;

    public MaybeRsaChannel(Channel channel, PrivateKey privateKey)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException {
        this.channel = channel;
        cipher = SecurityUtils.getCipher(SecurityUtils.RSA, Cipher.DECRYPT_MODE,
                privateKey, null);
    }

    @Override
    public byte[] encode(byte[] in) throws IOException {
        return channel.encode(in);
    }

    @Override
    public byte[] decode(byte[] in) throws IOException {
        byte[] b = channel.decode(in);

        try {
            byte[] dcrypt = cipher.doFinal(SecurityUtils.fromBase64(b));
            flags = channel.getFlags() | FLAG_ENCRYPTED;
            return dcrypt;
        } catch (Throwable t) {
            flags = channel.getFlags();
            return b;
        }
    }
}
