
package com.ds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.ds.channels.AesChannel;
import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
import com.ds.channels.MaybeRsaChannel;
import com.ds.channels.NopChannel;
import com.ds.channels.RsaChannel;
import com.ds.channels.Sha256OutChannel;
import com.ds.commands.Command;
import com.ds.commands.CommandBid;
import com.ds.commands.CommandChallenge;
import com.ds.commands.CommandCreate;
import com.ds.commands.CommandLogin;
import com.ds.loggers.Log;
import com.ds.responses.Response;
import com.ds.responses.Response.Rsp;
import com.ds.responses.ResponseAuctionCreated;
import com.ds.responses.ResponseAuctionList;
import com.ds.responses.ResponseClientList;
import com.ds.responses.ResponseOk;
import com.ds.server.UserList.User;
import com.ds.util.CommandMatcher;
import com.ds.util.SecurityUtils;

public class ServerThread implements Runnable {

    private final BufferedReader in;
    private final PrintWriter out;
    private Channel channel = null;

    private final int id;
    private final Server.Data serverData;
    private State state = new StateConnected(this);
    private boolean quit = false;
    private static final List<CommandMatcher> matchers;

    static {
        List<CommandMatcher> l = new ArrayList<CommandMatcher>();
        l.add(new CommandMatcher(CommandMatcher.Type.LOGIN, "^!login\\s+([a-zA-Z0-9_\\-]+)\\s+([a-zA-Z0-9/+]{43}=)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.LOGOUT, "^!logout\\s*$"));
        l.add(new CommandMatcher(CommandMatcher.Type.LIST, "^!list\\s*$"));
        l.add(new CommandMatcher(CommandMatcher.Type.CREATE, "^!create\\s+([0-9]+)\\s+(.+)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.BID, "^!bid\\s+([0-9]+)\\s+([0-9.]+)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.END, "^!end\\s*$"));
        l.add(new CommandMatcher(CommandMatcher.Type.CHALLENGE, "^([a-zA-Z0-9/+]{43}=)$"));
        l.add(new CommandMatcher(CommandMatcher.Type.GETCLIENTLIST, "^!getclientlist\\s*$"));
        matchers = Collections.unmodifiableList(l);
    }

    public ServerThread(int id, Socket socket, Server.Data serverData) throws IOException {
        this.id = id;
        this.serverData = serverData;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());

        try {
            this.channel = new MaybeRsaChannel(new NopChannel(), serverData.getServerKey());
        } catch (Throwable t) {
            throw new IOException(t);
        }

        Log.i("ServerThread %d created", id);
    }

    @Override
    public void run() {
        try {
            String s;
            while (!quit && (s = in.readLine()) != null) {

                byte[] bytes = channel.decode(s.getBytes());
                String msg = new String(bytes, Channel.CHARSET);

                /* Parse incoming command. */

                CommandMatcher matcher = null;
                List<String> matches = null;
                for (int i = 0; i < matchers.size(); i++) {
                    matcher = matchers.get(i);
                    matches = matcher.match(msg);
                    if (matches != null) {
                        break;
                    }
                }

                if (matches == null) {
                    Log.w("Invalid command '%s'", msg);
                    continue;
                }

                Log.i("Received command: %s", msg);

                Command cmd;
                try {
                    cmd = toCommand(msg, matcher.getType(), matches);
                } catch (IllegalArgumentException e) {
                    Log.w(e.getMessage());
                    continue;
                }

                state.processCommand(cmd);
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Log.e(e.getMessage());
            }
            out.close();
            state.cleanup();
        }

        Log.i("ServerThread %d shutting down", id);
    }

    /* The following methods are accessors for use by State classes. */

    private void setQuit() {
        quit = true;
    }

    private UserList getUserList() {
        return serverData.getUserList();
    }

    private AuctionList getAuctionList() {
        return serverData.getAuctionList();
    }

    private PublicKey getClientPublicKey(String client) {
        return serverData.getClientPublicKey(client);
    }

    private SecretKey getClientSecretKey(String client) {
        return serverData.getClientSecretKey(client);
    }

    public PrivateKey getServerKey() {
        return serverData.getServerKey();
    }

    private void setState(State state) {
        this.state = state;
    }

    private void sendResponse(Response response) {
        try {
            byte[] bytes = channel.encode(response.toNetString().getBytes());
            String str = new String(bytes, Channel.CHARSET);
            out.println(str);
            out.flush();
        } catch (IOException e) {
            Log.e("Could not write to channel");
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Extracts a Command object from the parsed incoming string.
     */
    private static Command toCommand(String cmd, CommandMatcher.Type type, List<String> args) {
        switch (type) {
        case LIST:
            return new Command(cmd, Command.Cmd.LIST);
        case LOGIN:
            return new CommandLogin(cmd, args.get(0), SecurityUtils.fromBase64(args.get(1).getBytes()));
        case LOGOUT:
            return new Command(cmd, Command.Cmd.LOGOUT);
        case BID:
            return new CommandBid(cmd, Integer.parseInt(args.get(0)), (int)Double.parseDouble(args.get(1)));
        case CREATE:
            return new CommandCreate(cmd, Integer.parseInt(args.get(0)), args.get(1));
        case END:
            return new Command(cmd, Command.Cmd.END);
        case CHALLENGE:
            return new CommandChallenge(SecurityUtils.fromBase64(args.get(0).getBytes()));
        case GETCLIENTLIST:
            return new Command(cmd, Command.Cmd.GETCLIENTLIST);
        default:
            throw new IllegalArgumentException("Could not parse command");
        }
    }

    /**
     * States are used to respond to incoming commands.
     */
    private interface State {
        void processCommand(Command command);
        void cleanup();
    }

    /**
     * StateConnected handles situations in which a user is connected,
     * but not logged in.
     */
    private static class StateConnected implements State {

        private final ServerThread serverThread;

        public StateConnected(ServerThread serverThread) {
            this.serverThread = serverThread;
        }

        @Override
        public void processCommand(Command command) {
            switch (command.getType()) {
            case LOGIN:
                CommandLogin commandLogin = (CommandLogin)command;

                /* Ensure that the incoming login command was encrypted. */

                int isEncrypted = serverThread.getChannel().getFlags() & Channel.FLAG_ENCRYPTED;
                if (isEncrypted == 0) {
                    Log.w("Unencrypted login request ignored.");
                    return;
                }

                try {
                    /* First, send the server challenge over an RSA channel. */

                    Channel b64c = new Base64Channel(new NopChannel());
                    Channel rsac = new RsaChannel(b64c,
                            serverThread.getClientPublicKey(commandLogin.getUser()),
                            serverThread.getServerKey());
                    serverThread.setChannel(rsac);

                    ResponseOk r = new ResponseOk(commandLogin.getChallenge());
                    serverThread.sendResponse(r);

                    /* Then, immediately switch to an AES channel to prepare for
                     * the incoming response. */

                    Channel hmac = new Sha256OutChannel(new NopChannel(),
                            serverThread.getClientSecretKey(commandLogin.getUser()));
                    b64c = new Base64Channel(hmac);
                    Channel aesc = new AesChannel(b64c, r.getSecretKey(), new IvParameterSpec(r.getIv()));
                    serverThread.setChannel(aesc);

                    serverThread.setState(new StateChallenge(
                            serverThread,
                            commandLogin.getUser(),
                            r.getServerChallenge()));
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return;
                }
                break;
            case GETCLIENTLIST:
                serverThread.sendResponse(new ResponseClientList(serverThread.getUserList()));
                break;
            case LIST:
                serverThread.sendResponse(new ResponseAuctionList(serverThread.getAuctionList()));
                break;
            case END:
                serverThread.setQuit();
                break;
            default:
                Log.e("Invalid command %s in connected state", command);
            }
        }

        @Override
        public void cleanup() {}

    }

    /**
     * StateChallenge is responsible for completing the authentication after
     * a client has requested to be logged in.
     */
    private static class StateChallenge implements State {

        private final byte[] challenge;
        private final ServerThread serverThread;
        private final String username;

        public StateChallenge(ServerThread serverThread, String username, byte[] challenge) {
            this.serverThread = serverThread;
            this.challenge = challenge;
            this.username = username;
        }

        @Override
        public void processCommand(Command command) {
            switch (command.getType()) {
            case CHALLENGE:
                CommandChallenge c = (CommandChallenge)command;

                if (!Arrays.equals(challenge, c.getChallenge())) {
                    Log.w("Client did not match server challenge");
                    break;
                }

                UserList userList = serverThread.getUserList();
                User user = userList.login(username);
                if (user == null) {
                    serverThread.sendResponse(new Response(Rsp.NAK));
                    Log.e("User %s login failed: already logged in", username);
                    break;
                }
                serverThread.setState(new StateRegistered(serverThread, user));
                Log.i("User %s logged in", user.getName());
                return;
            default:
                Log.e("Invalid command %s in challenged state", command);
                break;
            }

            /* On error, this part is reached. */

            serverThread.setChannel(new NopChannel());
            serverThread.setState(new StateConnected(serverThread));
        }

        @Override
        public void cleanup() {}

    }

    /**
     * StateRegistered is responsible for situations in which a user is logged in.
     */
    private static class StateRegistered implements State {

        private final ServerThread serverThread;

        private final User user;

        public StateRegistered(ServerThread serverThread, User user) {
            this.serverThread = serverThread;
            this.user = user;
        }

        @Override
        public void processCommand(Command command) {
            switch (command.getType()) {
            case GETCLIENTLIST:
                serverThread.sendResponse(new ResponseClientList(serverThread.getUserList()));
                break;
            case LIST:
                serverThread.sendResponse(new ResponseAuctionList(serverThread.getAuctionList()));
                break;
            case LOGOUT:
                logout();
                break;
            case CREATE:
                CommandCreate commandCreate = (CommandCreate)command;
                AuctionList auctionList = serverThread.getAuctionList();

                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.add(Calendar.SECOND, commandCreate.getDuration());

                int id = auctionList.add(commandCreate.getDescription(), user, now.getTime());
                serverThread.sendResponse(new ResponseAuctionCreated(id));
                break;
            case BID:
                CommandBid commandBid = (CommandBid)command;
                auctionList = serverThread.getAuctionList();

                auctionList.bid(commandBid.getAuctionId(), user, commandBid.getAmount());
                serverThread.sendResponse(new Response(Rsp.ACK));
                break;
            case END:
                logout();
                serverThread.setQuit();
                break;
            default:
                Log.e("Invalid command %s in registered state", command);
            }
        }

        @Override
        public void cleanup() {
            serverThread.getUserList().logout(user);
            Log.i("Connection lost, user %s logged out", user.getName());
        }

        private void logout() {
            UserList userList = serverThread.getUserList();
            if (!userList.logout(user)) {
                serverThread.sendResponse(new Response(Rsp.NAK));
                Log.e("User %s logout failed: not logged in", user.getName());
                return;
            }
            try {
                serverThread.setState(new StateConnected(serverThread));
                serverThread.setChannel(new MaybeRsaChannel(new NopChannel(), serverThread.getServerKey()));
                serverThread.sendResponse(new Response(Rsp.ACK));
                Log.i("User %s logged out", user.getName());
            } catch (Throwable t) {
                Log.e("Error while logging out: %s", t.getMessage());
            }
        }

    }

}
