package com.ds.channels;


//public class Base64ChannelImpl implements Channel {
//    private final TcpChannel tcpChannel;
//
//    public Base64ChannelImpl(TcpChannel tcpChannel) throws IOException {
//        this.tcpChannel = tcpChannel;
//    }
//
//    @Override
//    public void write(byte[] bytes) {
//        tcpChannel.printf(new String(SecurityUtils.toBase64(bytes)));
//    }
//
//    @Override
//    public byte[] read() throws IOException {
//        return SecurityUtils.fromBase64(tcpChannel.readLine().getBytes());
//    }
//
//    @Override
//    public void close() {
//        tcpChannel.close();
//    }
//}
