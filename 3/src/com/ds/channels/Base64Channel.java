package com.ds.channels;

import java.io.IOException;

import com.ds.util.SecurityUtils;

public class Base64Channel implements Channel {

    private final Channel channel;

    public Base64Channel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public byte[] encode(byte[] in) throws IOException {
        byte[] encoded = SecurityUtils.toBase64(in);
        return channel.encode(encoded);
    }

    @Override
    public byte[] decode(byte[] in) throws IOException {
        byte[] b = channel.decode(in);
        return SecurityUtils.fromBase64(b);
    }
}
