package com.ds.client;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.ds.util.SecurityUtils;

class Data {

    private final PublicKey serverKey;
    private final String clientKeyDir;
    private final int port;
    private final BlockingQueue<Parcel> processorQueue;
    private final BlockingQueue<TimeRequest> timeRetrieverQueue;
    private final List<Socket> sockets = new ArrayList<Socket>();
    private boolean done = false;

    public Data(ParsedArgs args) throws IOException {
        this.serverKey = SecurityUtils.readPublicKey(args.getServerPublicKey());
        this.clientKeyDir = args.getClientKeyDir();
        this.processorQueue = new LinkedBlockingQueue<Parcel>();
        this.timeRetrieverQueue = new LinkedBlockingQueue<TimeRequest>();
        this.port = args.getUdpPort();
    }

    public PublicKey getServerKey() {
        return serverKey;
    }

    public String getClientKeyDir() {
        return clientKeyDir;
    }

    public BlockingQueue<Parcel> getProcessorQueue() {
        return processorQueue;
    }

    public BlockingQueue<TimeRequest> getTimeRetrieverQueue() {
        return timeRetrieverQueue;
    }

    public int getPort() {
        return port;
    }

    public void addSocket(Socket socket) {
        synchronized(sockets) {
            if (sockets.contains(socket)) {
                return;
            }
            sockets.add(socket);
        }
    }

    public void removeSocket(Socket socket) {
        synchronized(sockets) {
            sockets.remove(socket);
        }
    }

    public List<Socket> getSockets() {
        synchronized (sockets) {
            return Collections.unmodifiableList(sockets);
        }
    }

    public synchronized boolean isDone() {
        return done;
    }

    public synchronized void setDone() {
        this.done = true;
    }

}
