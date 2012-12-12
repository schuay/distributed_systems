package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import com.ds.interfaces.Base64Channel;
import com.ds.interfaces.StringChannel;
import com.ds.util.SecurityUtils;

public class SecureChannel implements StringChannel {

    private final Base64Channel base64Channel;
    private final Cipher encCipher;
    private final Cipher decCipher;

    public SecureChannel(Base64Channel base64Channel, Key sessionKey, SecureRandom iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        this.base64Channel = base64Channel;
        this.encCipher = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.ENCRYPT_MODE,
                sessionKey, iv);
        this.decCipher = SecurityUtils.getCipher(SecurityUtils.AES, Cipher.DECRYPT_MODE,
                sessionKey, iv);
    }

    @Override
    public void printf(String format, Object... args) {
        String sendStr = String.format(format, args);
        base64Channel.printBytes(encCipher.update(sendStr.getBytes()));
    }

    @Override
    public String readLine() throws IOException {
        return new String(decCipher.update(base64Channel.readBytes()));
    }

    @Override
    public void close() {
        try {
            encCipher.doFinal();
        } catch (Exception e) {}

        try {
            decCipher.doFinal();
        } catch (Exception e) {}
    }
}
