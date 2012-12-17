package com.ds.client;

import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ds.util.SecurityUtils;

class Data {

    private final PublicKey serverKey;
    private final String clientKeyDir;
    private final BlockingQueue<Parcel> queue = new LinkedBlockingQueue<Parcel>();
    private final int port;

    public Data(ParsedArgs args) throws IOException {
        this.serverKey = SecurityUtils.readPublicKey(args.getServerPublicKey());
        this.clientKeyDir = args.getClientKeyDir();
        this.port = args.getUdpPort();
    }

    public PublicKey getServerKey() {
        return serverKey;
    }

    public String getClientKeyDir() {
        return clientKeyDir;
    }

    public BlockingQueue<Parcel> getQueue() {
        return queue;
    }

    public int getPort() {
        return port;
    }

}
