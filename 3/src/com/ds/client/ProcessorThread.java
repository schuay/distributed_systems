package com.ds.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.openssl.PasswordFinder;

import com.ds.channels.AesChannel;
import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
import com.ds.channels.NopChannel;
import com.ds.channels.RsaChannel;
import com.ds.channels.Sha256InChannel;
import com.ds.commands.Command;
import com.ds.commands.Command.Cmd;
import com.ds.commands.CommandBid;
import com.ds.commands.CommandChallenge;
import com.ds.commands.CommandLogin;
import com.ds.commands.CommandPassphrase;
import com.ds.commands.CommandRetry;
import com.ds.loggers.Log;
import com.ds.responses.Response;
import com.ds.responses.ResponseClientList;
import com.ds.responses.ResponseOk;
import com.ds.util.SecurityUtils;

public class ProcessorThread implements Runnable {

    private final Data data;
    private final BlockingQueue<Parcel> q; /* For convenience. */
    private PrintWriter out = null;
    private Channel channel = new NopChannel();
    private final List<User> users = new ArrayList<User>();
    private final Map<String, List<Command> > pendingSignedBids;

    public ProcessorThread(Data data) {
        this.data = data;
        this.q = data.getProcessorQueue();
        this.pendingSignedBids = new HashMap<String, List<Command> >();
    }

    @Override
    public void run() {
        try {
            State state = new StateDisconnected();

            while (!data.isDone()) {
                Parcel parcel = null;
                do {
                    try {
                        parcel = q.take();
                    } catch (InterruptedException e) {
                        Log.i("Interrupted while waiting for parcel: %s", e.getMessage());
                    }
                } while (parcel == null);

                switch (parcel.getType()) {
                case PARCEL_CONNECTION_ESTABLISHED:
                    SocketParcel socketParcel = (SocketParcel)parcel;
                    state = processParcelConnectionEstablished(socketParcel, state);
                    break;
                case PARCEL_CONNECTION_LOST:
                    state = processParcelConnectionLost(state);
                    break;
                case PARCEL_TERMINAL:
                    try {
                        StringParcel stringParcel = (StringParcel)parcel;
                        state = processParcelTerminal(stringParcel, state);
                    } catch (IllegalArgumentException e) {
                        Log.e(e.getMessage());
                    }
                    break;
                case PARCEL_NETWORK:
                    try {
                        StringParcel stringParcel = (StringParcel)parcel;
                        state = processParcelNetwork(stringParcel, state);
                    } catch (IOException e) {
                        Log.e(e.getMessage());
                    }
                    break;
                case PARCEL_TIMESTAMP_RESULT:
                    TimestampResultParcel timestampParcel = (TimestampResultParcel)parcel;

                    List<Command> l = pendingSignedBids.get(timestampParcel.getUser());
                    if (l == null) {
                        l = new LinkedList<Command>();
                        pendingSignedBids.put(timestampParcel.getUser(), l);
                    }

                    l.add(timestampParcel.getCommand());
                    break;
                default:
                    Log.w("Skipping parcel of unhandled type: %s", parcel.getType());
                    break;
                }
            }
        } finally {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }

    private State processParcelConnectionEstablished(SocketParcel socketParcel, State state) {
        return state.processConnectionEstablished(socketParcel.getSocket());
    }

    private State processParcelConnectionLost(State state) {
        return state.processConnectionLost();
    }

    private State processParcelTerminal(StringParcel stringParcel, State state) {
        Command cmd = Command.parse(stringParcel.getMessage());
        return state.processCommand(cmd);
    }

    private State processParcelNetwork(StringParcel stringParcel, State state) throws IOException {
        byte[] bytes = stringParcel.getMessage().getBytes(Channel.CHARSET);
        String in = new String(channel.decode(bytes), Channel.CHARSET);
        Response rsp = Response.parse(in);

        return state.processResponse(rsp);
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

    /**
     * The default state, which is responsible for parcels that should
     * be handled the same way in every state.
     */
    private abstract class State {

        public State processResponse(Response response) {
            switch (response.getResponse()) {
            case CLIENT_LIST:
                System.out.println(response.toString());
                System.out.flush();

                ResponseClientList r = (ResponseClientList)response;
                updateUserList(r);

                return this;

            default:
                System.out.println(response.toString());
                System.out.flush();
                return this;
            }
        }

        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case END:
                data.setDone();
                send(cmd.toString());
                break;
            case LIST:
                send(cmd.toString());
                break;
            default:
                Log.w("Command invalid in current state");
                break;
            }

            return this;
        }

        public State processConnectionEstablished(Socket s) {
            if (out != null) {
                out.close();
                out = null;
            }

            try {
                out = new PrintWriter(s.getOutputStream());
                Log.i("Connection established");

                channel = new NopChannel();

                return new StateLoggedOut();
            } catch (IOException e) {
                Log.e(e.getMessage());
            }

            return this;
        }

        public State processConnectionLost() {
            if (out != null) {
                out.close();
                out = null;
            }

            Log.i("Connection lost");

            return new StateDisconnected();
        }

        private void updateUserList(ResponseClientList r) {
            users.clear();
            for (String line : r.getClientList().split("\\n")) {
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    users.add(User.fromString(line));
                } catch (IllegalArgumentException e) {
                    Log.w("Received invalid user string: %s", line);
                }
            }
        }
    }

    /**
     * In this state, the user is logged out, and no connection is established.
     * The only possible transition is to StateLoggedOut.
     */
    private class StateDisconnected extends State {

        public StateDisconnected() {
            data.getTimeRetrieverQueue().add(new P2PLogoutTask());
        }

        @Override
        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case END:
                data.setDone();
                break;
            default:
                Log.w("Command invalid in current state");
                break;
            }

            return this;
        }
    }

    /**
     * Similar to {@link StateDisconnected}, except that a specific user is logged in and
     * may place offline bids which are verified by peers and forwarded to the server after the
     * next successful login.
     */
    private class StateDisconnectedButLoggedIn extends State {

        @Override
        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case BID:
                Log.i("Processing offline bid");
                data.getTimeRetrieverQueue().add(new P2PGetTimestampTask((CommandBid)cmd));
                break;
            case END:
                data.setDone();
                break;
            default:
                Log.w("Command invalid in current state");
                break;
            }

            return this;
        }
    }

    /**
     * In this state, the user is logged out, and is not in the middle of a handshake.
     */
    private class StateLoggedOut extends State {

        public StateLoggedOut() {
            data.getTimeRetrieverQueue().add(new P2PLogoutTask());
        }

        @Override
        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case LOGIN:
                try {
                    CommandLogin c = (CommandLogin)cmd;
                    c.setPort(data.getPort());
                    System.out.println("Please enter your passphrase using the '!pass <passphrase>' command");
                    return new StatePassphrase(c);
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return this;
                }
            default:
                return super.processCommand(cmd);
            }
        }
    }

    /**
     * The user has begun the login procedure, and we are now waiting for the passphrase.
     * The only way to exit this state is to either enter a passphrase or end the application.
     */
    private class StatePassphrase extends State {

        private final CommandLogin commandLogin;

        public StatePassphrase(CommandLogin commandLogin) {
            this.commandLogin = commandLogin;
        }

        @Override
        public State processCommand(Command cmd) {
            switch (cmd.getType()) {
            case PASSPHRASE:
                try {
                    CommandPassphrase c = (CommandPassphrase)cmd;

                    /* Encrypt the login command with the server's public key. */

                    PrivateKey key = readPrivateKey(commandLogin.getUser(), c.getPassphrase());

                    Channel b64c = new Base64Channel(new NopChannel());
                    Channel rsac = new RsaChannel(b64c, data.getServerKey(), key);

                    channel = rsac;

                    send(commandLogin.toString());
                    return new StateChallenge(commandLogin.getUser(),
                            commandLogin.getChallenge(), key);
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return this;
                }
            case END:
                return super.processCommand(cmd);
            default:
                Log.w("Passphrase expected but other command received");
                return this;
            }
        }
    }

    private PrivateKey readPrivateKey(String user, final String passphrase) throws IOException {
        File dir = new File(data.getClientKeyDir());
        File file = new File(dir, String.format("%s.pem",  user));

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Could not read private key");
        }

        PrivateKey key = SecurityUtils.readPrivateKey(file.getAbsolutePath(),
                new PasswordFinder() {
            @Override
            public char[] getPassword() {
                return passphrase.toCharArray();
            }
        });

        return key;
    }

    private SecretKey readSecretKey(String user) throws IOException {
        File dir = new File(data.getClientKeyDir());
        File file = new File(dir, String.format("%s.key",  user));

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Could not read secret key");
        }

        return SecurityUtils.readSecretKey(file.getAbsolutePath(), SecurityUtils.AES);
    }

    /**
     * The login message has been sent. We are waiting for the server response,
     * which includes the client challenge and the server challenge.
     * All user input is ignored until we move into another state.
     */
    private class StateChallenge extends State {

        private final byte[] challenge;
        private final String user;
        private final PrivateKey key;

        public StateChallenge(String user, byte[] challenge, PrivateKey key) {
            this.user = user;
            this.key = key;
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

                    SecretKey k = readSecretKey(user);

                    Channel hmac = new Sha256InChannel(new NopChannel(), k);
                    Channel b64c = new Base64Channel(hmac);
                    Channel aesc = new AesChannel(b64c, r.getSecretKey(),
                            new IvParameterSpec(r.getIv()));

                    CommandChallenge c = new CommandChallenge(r.getServerChallenge());

                    channel = aesc;

                    send(c.toString());

                    return new StateLoggedIn(user, key);
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

    /**
     * We are logged in.
     */
    private class StateLoggedIn extends State {

        private boolean pendingRetry = false;
        private boolean blocked = false;

        public StateLoggedIn(String user, PrivateKey key) {
            data.getTimeRetrieverQueue().add(new P2PLoginTask(user, key));
            send(new Command("!getclientlist", Cmd.GETCLIENTLIST).toString());

            /* Send signed bids. */

            List<Command> l = pendingSignedBids.remove(user);
            if (l != null) {
                for (Command c : l) {
                    send(c.toString());
                }
            }
        }

        @Override
        public State processCommand(Command cmd) {
            if (blocked) {
                Log.w("Waiting for group bid confirmation or rejection, command ignored");
                return this;
            }

            switch (cmd.getType()) {
            case CONFIRM:
                blocked  = true;
                send(cmd.toString());
                return this;
            case LOGOUT:
                send(cmd.toString());
                channel = new NopChannel();
                return new StateLoggedOut();
            case BID:
            case CREATE:
            case GETCLIENTLIST:
            case GROUPBID:
            case SIGNEDBID:
                send(cmd.toString());
                return this;
            default:
                return super.processCommand(cmd);
            }
        }

        @Override
        public State processResponse(Response response) {

            /* If a message is mangled, print it but do not process it further in any way.
             * Request retransmission if this is the first such message received in sequence,
             * otherwise reset our pendingRetry state.
             */

            boolean isMangled = ((channel.getFlags() & Channel.FLAG_MANGLED) != 0);
            if (isMangled) {
                System.out.printf("Mangled server response:%n%s", response.toNetString());

                if (!pendingRetry ) {
                    pendingRetry = true;
                    send(new CommandRetry().toString());
                } else {
                    pendingRetry = false;
                }

                return this;
            }
            pendingRetry = false;

            switch (response.getResponse()) {
            case CONFIRMED:
                Log.i("Group bid confirmed");
                blocked = false;
                return this;
            case REJECTED:
                Log.i("Group bid rejected");
                blocked = false;
                return this;
            default:
                return super.processResponse(response);
            }
        }

        @Override
        public State processConnectionLost() {
            if (out != null) {
                out.close();
                out = null;
            }

            Log.i("Connection lost");

            return new StateDisconnectedButLoggedIn();
        }
    }
}
