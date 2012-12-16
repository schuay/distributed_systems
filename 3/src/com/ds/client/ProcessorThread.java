package com.ds.client;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import com.ds.commands.Command;
import com.ds.loggers.Log;

public class ProcessorThread implements Runnable {

    private final Data data;
    private final BlockingQueue<Parcel> q; /* For convenience. */
    private final Socket socket;
    private boolean keepGoing = true;

    public ProcessorThread(Socket socket, Data data) {
        this.socket = socket;
        this.data = data;
        this.q = data.getQueue();
    }

    @Override
    public void run() {
        while (keepGoing) {
            Parcel parcel = null;
            do {
                try {
                    parcel = q.take();
                } catch (InterruptedException e) {
                    Log.i("Interrupted while waiting for parcel: %s", e.getMessage());
                }
            } while (parcel == null);

            switch (parcel.getType()) {
            case PARCEL_TERMINAL:
                processTerminalParcel(parcel);
                break;
            case PARCEL_NETWORK:
                processNetworkParcel(parcel);
                break;
            default:
                break;
            }
        }
    }

    private void processNetworkParcel(Parcel parcel) {
        System.out.println(parcel.getMessage());
    }

    private void processTerminalParcel(Parcel parcel) {
        Command cmd = null;
        try {
            cmd = Command.parse(parcel.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e("Invalid command: %s", parcel.getMessage());
            return;
        }

        switch (cmd.getType()) {
        case END:
            keepGoing = false;
            break;
        default:
            System.out.println(parcel.getMessage());
            break;
        }
    }

}
