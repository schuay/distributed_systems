package com.ds.client;

import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.Semaphore;

import com.ds.channels.Channel;
import com.ds.channels.TcpChannel;
import com.ds.util.SecurityUtils;

class Data {

    private Channel channel;
    private TcpChannel tcpChannel;
    private final PublicKey serverKey;
    private final String clientKeyDir;
    private final Semaphore semaphore = new Semaphore(1);

    public Data(TcpChannel tcpChannel, ParsedArgs args) throws IOException {
        this.channel = this.tcpChannel = tcpChannel;
        this.serverKey = SecurityUtils.readPublicKey(args.getServerPublicKey());
        this.clientKeyDir = args.getClientKeyDir();
    }

    public synchronized void setChannel(Channel channel) {
        if (this.channel != null && this.channel != tcpChannel) {
            this.channel.close();
        }
        this.channel = channel;
    }

    public synchronized void resetChannel() {
        setChannel(tcpChannel);
    }

    public synchronized Channel getChannel() {
        return channel;
    }

    public PublicKey getServerKey() {
        return serverKey;
    }

    public String getClientKeyDir() {
        return clientKeyDir;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

}
