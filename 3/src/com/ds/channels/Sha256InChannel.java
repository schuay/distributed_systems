package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import com.ds.util.SecurityUtils;

public class Sha256InChannel extends Channel {

    private final Channel b64c = new Base64Channel(new NopChannel());
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
            byte[] b64hmac = msgAndHmac.substring(indexOfLastSpace + 1).getBytes(Channel.CHARSET);
            byte[] hmac = b64c.decode(b64hmac);

            boolean isEqual = SecurityUtils.verifyHMAC(key, SecurityUtils.SHA256, hmac, msg);
            if (!isEqual) {
                /* TODO: Do this in such a way that can be easily recognized by the catcher. */
                setFlags(channel.getFlags() | FLAG_MANGLED);
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
