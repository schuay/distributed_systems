package com.ds.channels;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.util.SecurityUtils;


public class AesChannel extends Channel {

    private final Channel channel;
    private final Cipher ecrypt;
    private final Cipher dcrypt;

    public AesChannel(Channel channel, Key sessionKey, AlgorithmParameterSpec iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException {
        this.channel = channel;
        ecrypt = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.ENCRYPT_MODE, sessionKey, iv);
        dcrypt = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.DECRYPT_MODE, sessionKey, iv);
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
        } catch (IOException e) {
            throw e;
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }
}
