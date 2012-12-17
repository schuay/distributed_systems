package com.ds.channels;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import com.ds.util.SecurityUtils;

public class Sha256OutChannel implements Channel {

    private final Channel b64c = new Base64Channel(new NopChannel());
    private final Channel channel;
    private final SecretKey key;

    public Sha256OutChannel(Channel channel, SecretKey key) {
        this.channel = channel;
        this.key = key;
    }

    @Override
    public byte[] encode(byte[] in) throws IOException {
        try {
            byte[] hmac = SecurityUtils.getHMAC(key, SecurityUtils.SHA256, in);
            byte[] b64hmac = b64c.encode(hmac);
            String out = String.format("%s %s", new String(in, Channel.CHARSET),
                    new String(b64hmac, Channel.CHARSET));
            return channel.encode(out.getBytes(Channel.CHARSET));
        } catch (InvalidKeyException e) {
            throw new IOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    @Override
    public byte[] decode(byte[] in) throws IOException {
        return channel.decode(in);
    }

}
