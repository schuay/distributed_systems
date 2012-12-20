package com.ds.client;

import java.net.Socket;


public class SocketParcel extends Parcel {

    private final Socket socket;

    public SocketParcel(Type type, Socket socket) {
        super(type);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

}
