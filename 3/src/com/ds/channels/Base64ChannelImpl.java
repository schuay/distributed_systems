package com.ds.channels;

import java.io.IOException;

import com.ds.interfaces.Base64Channel;
import com.ds.util.SecurityUtils;

public class Base64ChannelImpl implements Base64Channel {
    private final TcpChannel tcpChannel;

    public Base64ChannelImpl(TcpChannel tcpChannel) throws IOException {
        this.tcpChannel = tcpChannel;
    }

    @Override
    public void printBytes(byte[] bytes) {
        tcpChannel.printf(new String(SecurityUtils.toBase64(bytes)));
    }

    @Override
    public byte[] readBytes() throws IOException {
        return SecurityUtils.fromBase64(tcpChannel.readLine().getBytes());
    }
}
