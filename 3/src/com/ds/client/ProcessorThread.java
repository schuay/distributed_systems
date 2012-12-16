package com.ds.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.concurrent.BlockingQueue;

import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
import com.ds.channels.NopChannel;
import com.ds.channels.RsaChannel;
import com.ds.commands.Command;
import com.ds.commands.CommandLogin;
import com.ds.loggers.Log;
import com.ds.util.SecurityUtils;

public class ProcessorThread implements Runnable {

    private final Data data;
    private final BlockingQueue<Parcel> q; /* For convenience. */
    private boolean keepGoing = true;
    private final PrintWriter out;
    private Channel channel = new NopChannel();

    public ProcessorThread(Socket socket, Data data) throws IOException {
        this.out = new PrintWriter(socket.getOutputStream());
        this.data = data;
        this.q = data.getQueue();
    }

    @Override
    public void run() {
        try {
            State state = new StateLoggedOut();

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
            out.flush();
        } catch (UnsupportedEncodingException e) {
            Log.e(e.getMessage());
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
    }

    private static PrivateKey readPrivateKey(String clientKeyDir, String user) throws IOException {
        File dir = new File(clientKeyDir);
        File file = new File(dir, String.format("%s.pem", user));

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Could not read private key");
        }

        return SecurityUtils.readPrivateKey(file.getAbsolutePath());
    }

    /* The default state, which is responsible for parcels that should
     * be handled the same way in every state.
     */
    private abstract class State {

        public State processNetworkParcel(Parcel parcel) {
            System.out.println(parcel.getMessage());
            System.out.flush();
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
                send(cmd.toString());
                break;
            }

            return this;
        }
    }

    private class StateLoggedOut extends State {

        @Override
        public State processTerminalParcel(Parcel parcel) {
            Command cmd = null;
            try {
                cmd = Command.parse(parcel.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e("Invalid command: %s", parcel.getMessage());
                return this;
            }

            switch (cmd.getType()) {
            case LOGIN:
                try {
                    CommandLogin c = (CommandLogin)cmd;

                    /* Encrypt the login command with the server's public key. */

                    PrivateKey key = readPrivateKey(data.getClientKeyDir(), c.getUser());
                    Channel b64c = new Base64Channel(new NopChannel());
                    Channel rsac = new RsaChannel(b64c, data.getServerKey(), key);

                    channel = rsac;

                    send(c.toString());
                    return new StateChallenge();
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return this;
                }
            default:
                return super.processTerminalParcel(parcel);
            }
        }

        private class StateChallenge extends State {

            @Override
            public State processTerminalParcel(Parcel parcel) {
                Command cmd = null;
                try {
                    cmd = Command.parse(parcel.getMessage());
                } catch (IllegalArgumentException e) {
                    Log.e("Invalid command: %s", parcel.getMessage());
                    return this;
                }

                switch (cmd.getType()) {
                case LOGOUT:
                    send(cmd.toString());
                    channel = new NopChannel();
                    return new StateLoggedOut();
                default:
                    return super.processTerminalParcel(parcel);
                }
            }
        }

    }
}
