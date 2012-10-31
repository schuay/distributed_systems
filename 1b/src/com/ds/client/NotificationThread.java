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
                printNotification(new String(packet.getData(), 0, packet.getLength()));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void printNotification(String notification) {
        String[] tokens = notification.split(" ");
        if (tokens[0].equals("!new-bid")) {
            if (tokens.length < 2) {
                return;
            }
            System.out.printf("You have been overbid on ");
            for (int i = 1; i < tokens.length; i++) {
                System.out.printf("%s ", tokens[i]);
            }
            System.out.println();
        } else if (tokens[0].equals("!auction-ended")) {
            if (tokens.length < 4) {
                return;
            }
            System.out.printf("The auction ");
            for (int i = 3; i < tokens.length; i++) {
                System.out.printf("%s ", tokens[i]);
            }
            System.out.printf("has ended. %s won with %s%n", tokens[1], tokens[2]);
        }
    }

    public void setQuit() {
        socket.close();
    }

}
