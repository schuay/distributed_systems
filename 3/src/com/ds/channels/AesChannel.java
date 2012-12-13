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

    private final Base64Channel base64Channel;
    private final Cipher encCipher;
    private final Cipher decCipher;

    public AesChannel(Base64Channel base64Channel, Key sessionKey, SecureRandom iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        this.base64Channel = base64Channel;
        this.encCipher = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.ENCRYPT_MODE,
                sessionKey, iv);
        this.decCipher = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.DECRYPT_MODE,
                sessionKey, iv);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        byte[] encrypted = encCipher.update(bytes);
        base64Channel.write(encrypted);
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), TcpChannel.CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        return decCipher.update(base64Channel.read());
    }

    @Override
    public void close() {
        try {
            encCipher.doFinal();
            decCipher.doFinal();
        } catch (Exception e) {
            Log.e(e.getMessage());
        }
    }
}
