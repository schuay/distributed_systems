package com.ds.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import com.ds.channels.Channel;
import com.ds.channels.NopChannel;
import com.ds.commands.Command;
import com.ds.loggers.Log;

public class ProcessorThread implements Runnable {

    private final Data data;
    private final BlockingQueue<Parcel> q; /* For convenience. */
    private boolean keepGoing = true;
    private final PrintWriter out;
    private final Channel channel = new NopChannel();

    public ProcessorThread(Socket socket, Data data) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream());
        this.data = data;
        this.q = data.getQueue();
    }

    @Override
    public void run() {
        try {
            State state = new State();

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
                    state = state.processTerminalParcel(parcel);
                    break;
                case PARCEL_NETWORK:
                    state = state.processNetworkParcel(parcel);
                    break;
                default:
                    break;
                }
            }
        } finally {
            out.close();
        }
    }

    private void send(String in) {
        try {
            out.println(new String(channel.encode(in.getBytes(Channel.CHARSET)), Channel.CHARSET));
        } catch (UnsupportedEncodingException e) {
            Log.e(e.getMessage());
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
    }

    /* The default state, which is responsible for parcels that should
     * be handled the same way in every state.
     */
    private class State {

        public State processNetworkParcel(Parcel parcel) {
            System.out.println(parcel.getMessage());
            return this;
        }

        public State processTerminalParcel(Parcel parcel) {
            Command cmd = null;
            try {
                cmd = Command.parse(parcel.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e("Invalid command: %s", parcel.getMessage());
                return this;
            }

            switch (cmd.getType()) {
            case END:
                keepGoing = false;
                send(cmd.toString());
                break;
            default:
                System.out.println(parcel.getMessage());
                break;
            }

            return this;
        }
    }
}
