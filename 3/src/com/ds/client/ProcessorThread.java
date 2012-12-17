package com.ds.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.openssl.PasswordFinder;

import com.ds.channels.AesChannel;
import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
import com.ds.channels.NopChannel;
import com.ds.channels.RsaChannel;
import com.ds.commands.Command;
import com.ds.commands.CommandChallenge;
import com.ds.commands.CommandLogin;
import com.ds.loggers.Log;
import com.ds.responses.Response;
import com.ds.responses.ResponseOk;
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
                    Command cmd = null;
                    try {
                        cmd = Command.parse(parcel.getMessage());
                    } catch (IllegalArgumentException e) {
                        Log.e("Invalid command: %s", parcel.getMessage());
                        continue;
                    }

                    state = state.processCommand(cmd);
                    break;
                case PARCEL_NETWORK:
                    Response rsp = null;
                    try {
                        byte[] bytes = parcel.getMessage().getBytes(Channel.CHARSET);
                        String in = new String(channel.decode(bytes), Channel.CHARSET);
                        rsp = Response.parse(in);
                    } catch (IllegalArgumentException e) {
                        Log.e("Invalid command: %s", parcel.getMessage());
                        continue;
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Invalid command: %s", parcel.getMessage());
                        continue;
                    } catch (IOException e) {
                        Log.e("Invalid command: %s", parcel.getMessage());
                        continue;
                    }
                    state = state.processResponse(rsp);
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

    /* The default state, which is responsible for parcels that should
     * be handled the same way in every state.
     */
    private abstract class State {

        public State processResponse(Response response) {
            System.out.println(response.toNetString());
            System.out.flush();
            return this;
        }

        public State processCommand(Command cmd) {
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
        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case LOGIN:
                try {
                    CommandLogin c = (CommandLogin)cmd;

                    /* Encrypt the login command with the server's public key. */

                    /* TODO: We are currently running into issues here, because another
                     * thread is listening to stdin while we are attempting to read the passphrase
                     * in readPrivateKey().
                     * Work around that for now.
                     */

                    File dir = new File(data.getClientKeyDir());
                    File file = new File(dir, String.format("%s.pem",  c.getUser()));

                    if (!file.exists() || !file.isFile()) {
                        throw new IllegalArgumentException("Could not read private key");
                    }

                    PrivateKey key = SecurityUtils.readPrivateKey(file.getAbsolutePath(),
                            new PasswordFinder() {
                        @Override
                        public char[] getPassword() {
                            Log.w("Using key entry workaround");
                            return "12345".toCharArray();
                        }
                    });

                    Channel b64c = new Base64Channel(new NopChannel());
                    Channel rsac = new RsaChannel(b64c, data.getServerKey(), key);

                    channel = rsac;

                    send(c.toString());
                    return new StateChallenge(c.getUser(), c.getChallenge());
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return this;
                }
            default:
                return super.processCommand(cmd);
            }
        }

        private class StateChallenge extends State {

            private final byte[] challenge;
            private final String user;

            public StateChallenge(String user, byte[] challenge) {
                this.user = user;
                this.challenge = challenge;
            }

            @Override
            public State processResponse(Response response) {
                switch (response.getResponse()) {
                case OK:
                    ResponseOk r = (ResponseOk)response;

                    if (!Arrays.equals(challenge, r.getClientChallenge())) {
                        /* TODO: Notify server of failure. */
                        Log.e("Challenge mismatch");
                        return new StateLoggedOut();
                    }

                    try {
                        /* Set up the AES channel. */

                        Channel b64c = new Base64Channel(new NopChannel());
                        Channel aesc = new AesChannel(b64c, r.getSecretKey(),
                                new IvParameterSpec(r.getIv()));

                        CommandChallenge c = new CommandChallenge(r.getServerChallenge());

                        channel = aesc;

                        send(c.toString());
                        return new StateLoggedIn();
                    } catch (Throwable t) {
                        Log.e(t.getMessage());
                        return new StateLoggedOut();
                    }
                case NAK:
                    Log.w("Login refused by server");
                    channel = new NopChannel();
                    return new StateLoggedOut();
                default:
                    return super.processResponse(response);
                }
            }

            @Override
            public State processCommand(Command cmd) {
                Log.w("Cannot process user input during handshake");
                return this;
            }
        }

        private class StateLoggedIn extends State {

            @Override
            public State processCommand(Command cmd) {
                switch (cmd.getType()) {
                case LOGOUT:
                    send(cmd.toString());
                    channel = new NopChannel();
                    return new StateLoggedOut();
                default:
                    return super.processCommand(cmd);
                }
            }
        }

    }
}