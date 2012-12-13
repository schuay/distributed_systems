package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.loggers.Log;
import com.ds.util.SecurityUtils;


public class AesChannel implements Channel {

    private final Channel channel;
    private final Cipher ecrypt;
    private final Cipher dcrypt;

    public AesChannel(Channel channel, Key sessionKey, SecureRandom iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
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
        return dcrypt.update(channel.read());
    }

    @Override
    public void close() {
        try {
            ecrypt.doFinal();
            dcrypt.doFinal();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }
}
