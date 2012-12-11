
package com.ds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;

import com.ds.common.Command;
import com.ds.common.CommandBid;
import com.ds.common.CommandCreate;
import com.ds.common.CommandLogin;
import com.ds.common.Response;
import com.ds.common.Response.Rsp;
import com.ds.common.ResponseAuctionCreated;
import com.ds.common.ResponseAuctionList;
import com.ds.loggers.Log;
import com.ds.server.UserList.User;

public class ServerThread implements Runnable {

    private final BufferedReader in;
    private final PrintWriter out;
    private final int id;
    private final Server.Data serverData;
    private State state = new StateConnected(this);
    private boolean quit = false;

    public ServerThread(int id, Socket socket, Server.Data serverData) throws IOException {
        this.id = id;
        this.serverData = serverData;

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream());

        Log.i("ServerThread %d created", id);
    }

    @Override
    public void run() {
        try {
            String msg;
            while (!quit && (msg = in.readLine()) != null) {
                Log.i("Received command: %s", msg);
                /* state.processCommand(command); */
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        out.write(response.toString());
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
