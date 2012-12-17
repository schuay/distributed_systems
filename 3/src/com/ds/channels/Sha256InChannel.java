package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import com.ds.util.SecurityUtils;

public class Sha256InChannel implements Channel {

    private final Channel channel;
    private final SecretKey key;

    public Sha256InChannel(Channel channel, SecretKey key) {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public byte[] encode(byte[] in) throws IOException {
        return channel.encode(in);
    }

    @Override
    public byte[] decode(byte[] in) throws IOException {
        try {
            byte[] b = channel.decode(in);

            String msgAndHmac = new String(b, Channel.CHARSET);
            int indexOfLastSpace = msgAndHmac.lastIndexOf(' ');
            byte[] msg = msgAndHmac.substring(0, indexOfLastSpace).getBytes(Channel.CHARSET);
            byte[] hmac = msgAndHmac.substring(indexOfLastSpace + 1).getBytes(Channel.CHARSET);

            boolean isEqual = SecurityUtils.verifyHMAC(key, SecurityUtils.SHA256, hmac, in);
            if (!isEqual) {
                /* TODO: Do this in such a way that can be easily recognized by the catcher. */
                throw new IOException("Hmac Mismatch");
            }

            return msg;
        } catch (InvalidKeyException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

}
