package com.ds.channels;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.loggers.Log;
import com.ds.util.SecurityUtils;


public class AesChannel implements Channel {

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
    public void write(byte[] bytes) throws IOException {
        byte[] encrypted = ecrypt.update(bytes);
        channel.write(encrypted);
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), TcpChannel.CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        try {
            /* TODO: This only decrypts a part of the sent message.
             * The bytes passed to doFinal seem to be correct..
             */
            return dcrypt.doFinal(channel.read());
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override
    public void close() {
        /* Empty. */
    }
}
