package com.ds.channels;

import java.io.IOException;

import com.ds.util.SecurityUtils;

public class Base64Channel implements Channel {

    private final Channel channel;

    public Base64Channel(Channel channel) throws IOException {
        this.channel = channel;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        byte[] encoded = SecurityUtils.toBase64(bytes);
        channel.write(encoded);
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), TcpChannel.CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        String encoded = channel.readLine();
        return SecurityUtils.fromBase64(encoded.getBytes());
    }

    @Override
    public void close() { }
}
