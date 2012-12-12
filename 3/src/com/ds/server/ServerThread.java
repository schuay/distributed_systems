
package com.ds.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ds.common.Command;
import com.ds.common.CommandBid;
import com.ds.common.CommandCreate;
import com.ds.common.CommandLogin;
import com.ds.common.Response;
import com.ds.common.Response.Rsp;
import com.ds.common.ResponseAuctionCreated;
import com.ds.common.ResponseAuctionList;
import com.ds.interfaces.StringChannel;
import com.ds.loggers.Log;
import com.ds.server.UserList.User;
import com.ds.util.CommandMatcher;

public class ServerThread implements Runnable {

    private final StringChannel channel;
    private final int id;
    private final Server.Data serverData;
    private State state = new StateConnected(this);
    private boolean quit = false;
    private final List<CommandMatcher> matchers;

    public ServerThread(int id, StringChannel channel, Server.Data serverData) throws IOException {
        this.id = id;
        this.serverData = serverData;
        this.channel = channel;

        /* Configure matchers. */

        matchers = new ArrayList<CommandMatcher>();
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGIN, "^!login\\s+([a-zA-Z0-9_\\-]+)\\s+([a-zA-Z0-9/+]{43}=)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.LOGOUT, "^!logout\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.LIST, "^!list\\s*$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.CREATE, "^!create\\s+([0-9]+)\\s+(.+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.BID, "^!bid\\s+([0-9]+)\\s+([0-9.]+)$"));
        matchers.add(new CommandMatcher(CommandMatcher.Type.END, "^!end\\s*$"));

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
        channel.printf(response.toNetString());
    }

    /**
     * Extracts a Command object from the parsed incoming string.
     */
    private static Command toCommand(String cmd, CommandMatcher.Type type, List<String> args) {
        switch (type) {
        case LIST:
            return new Command(cmd, Command.Cmd.LIST);
        case LOGIN:
            return new CommandLogin(cmd, args.get(0), args.get(1));
        case LOGOUT:
            return new Command(cmd, Command.Cmd.LOGOUT);
        case BID:
            return new CommandBid(cmd, Integer.parseInt(args.get(0)), (int)Double.parseDouble(args.get(1)));
        case CREATE:
            return new CommandCreate(cmd, Integer.parseInt(args.get(0)), args.get(1));
        case END:
            return new Command(cmd, Command.Cmd.END);
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
                UserList userList = serverThread.getUserList();
                User user = userList.login(commandLogin.getUser());
                if (user == null) {
                    serverThread.sendResponse(new Response(Rsp.ERROR));
                    Log.e("User %s login failed: already logged in", commandLogin.getUser());
                    return;
                }
                serverThread.setState(new StateRegistered(serverThread, user));
                serverThread.sendResponse(new Response(Rsp.OK));
                Log.i("User %s logged in", user.getName());
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
            serverThread.sendResponse(new Response(Rsp.OK));
            Log.i("User %s logged out", user.getName());
        }

    }

}
