package com.ds.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class NotificationThread implements Runnable {

    private final DatagramSocket socket;
    private static final int PACKET_LENGTH = 256;

    public NotificationThread(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        byte[] bytes = new byte[PACKET_LENGTH];
        DatagramPacket packet = new DatagramPacket(bytes, PACKET_LENGTH);

        try {
            while (true) {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setQuit() {
        socket.close();
    }

}
