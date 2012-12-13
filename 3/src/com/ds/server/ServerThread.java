
package com.ds.server;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ds.channels.AesChannel;
import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
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
import com.ds.responses.ResponseChallenge;
import com.ds.server.UserList.User;
import com.ds.util.CommandMatcher;

public class ServerThread implements Runnable {

    private final Channel baseChannel;
    private Channel channel;
    private final int id;
    private final Server.Data serverData;
    private State state = new StateConnected(this);
    private boolean quit = false;
    private final List<CommandMatcher> matchers;

    public ServerThread(int id, Channel channel, Server.Data serverData) throws IOException {
        this.id = id;
        this.serverData = serverData;
        this.channel = this.baseChannel = channel;

        /* Configure matchers. */

        matchers = new ArrayList<CommandMatcher>();
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGIN, "^!login\\s+([a-zA-Z0-9_\\-]+)\\s+([a-zA-Z0-9/+]{43}=)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGOUT, "^!logout\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.LIST, "^!list\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.CREATE, "^!create\\s+([0-9]+)\\s+(.+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.BID, "^!bid\\s+([0-9]+)\\s+([0-9.]+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.END, "^!end\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.CHALLENGE, "^([a-zA-Z0-9/+]{43}=)$"));

        Log.i("ServerThread %d created", id);
    }

    @Override
    public void run() {
        try {
            String msg;
            while (!quit && (msg = channel.readLine()) != null) {

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
            channel.close();
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

    private void setState(State state) {
        this.state = state;
    }

    private void sendResponse(Response response) {
        try {
            channel.write(response.toNetString().getBytes());
        } catch (IOException e) {
            Log.e("Could not write to channel");
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        if (this.channel != null && this.channel != this.baseChannel) {
            this.channel.close();
        }
        this.channel = channel;
    }

    public void resetChannel() {
        setChannel(baseChannel);
    }

    /**
     * Extracts a Command object from the parsed incoming string.
     */
    private static Command toCommand(String cmd, CommandMatcher.Type type, List<String> args) {
        switch (type) {
        case LIST:
            return new Command(cmd, Command.Cmd.LIST);
        case LOGIN:
            return new CommandLogin(cmd, args.get(0), args.get(1).getBytes()); /* TODO: Extract challenge. */
        case LOGOUT:
            return new Command(cmd, Command.Cmd.LOGOUT);
        case BID:
            return new CommandBid(cmd, Integer.parseInt(args.get(0)), (int)Double.parseDouble(args.get(1)));
        case CREATE:
            return new CommandCreate(cmd, Integer.parseInt(args.get(0)), args.get(1));
        case END:
            return new Command(cmd, Command.Cmd.END);
        case CHALLENGE:
            return new CommandChallenge(args.get(0).getBytes()); /* TODO: Extract challenge. */
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
            switch (command.getId()) {
            case LOGIN:
                CommandLogin commandLogin = (CommandLogin)command;

                try {
                    ResponseChallenge r = new ResponseChallenge(commandLogin.getChallenge());
                    serverThread.sendResponse(r);

                    Channel b64c = new Base64Channel(serverThread.getChannel());
                    Channel aesc = new AesChannel(b64c, r.getSecretKey(), new SecureRandom(r.getIv()));
                    serverThread.setChannel(aesc);

                    serverThread.setState(new StateChallenge(
                            serverThread,
                            commandLogin.getUser(),
                            commandLogin.getChallenge()));
                } catch (Throwable t) {
                    Log.e(t.getMessage());
                    return;
                }
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
            switch (command.getId()) {
            case CHALLENGE:
                CommandChallenge c = (CommandChallenge)command;

                if (!(challenge.equals(c.getChallenge()))) {
                    Log.w("Client did not match server challenge");
                    break;
                }

                UserList userList = serverThread.getUserList();
                User user = userList.login(username);
                if (user == null) {
                    serverThread.sendResponse(new Response(Rsp.ERROR));
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

            serverThread.resetChannel();
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
            switch (command.getId()) {
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
                serverThread.sendResponse(new Response(Rsp.OK));
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
                serverThread.sendResponse(new Response(Rsp.ERROR));
                Log.e("User %s logout failed: not logged in", user.getName());
                return;
            }
            serverThread.setState(new StateConnected(serverThread));
            serverThread.resetChannel();
            serverThread.sendResponse(new Response(Rsp.OK));
            Log.i("User %s logged out", user.getName());
        }

    }

}
