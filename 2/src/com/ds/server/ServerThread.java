
package com.ds.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import com.ds.server.UserList.User;

public class ServerThread implements Runnable {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final int id;
    private final Server.Data serverData;
    private State state = new StateConnected(this);
    private boolean quit = false;

    public ServerThread(int id, Socket socket, Server.Data serverData) throws IOException {
        this.id = id;
        this.serverData = serverData;

        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());

        System.out.printf("ServerThread %d created%n", id);
    }

    @Override
    public void run() {
        try {
            Command command;
            while (!quit && (command = (Command)in.readObject()) != null) {
                System.out.printf("Received command: %s%n", command);
                state.processCommand(command);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.printf("ServerThread %d shutting down%n", id);
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

    private void sendResponse(Object object) {
        try {
            out.writeObject(object);
        } catch (IOException e) {
            setQuit();
            e.printStackTrace();
        }
    }

    /**
     * States are used to respond to incoming commands.
     */
    private interface State {
        void processCommand(Command command);
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
                        System.out.printf("User %s login failed: already logged in%n", commandLogin.getUser());
                        return;
                    }
                    serverThread.setState(new StateRegistered(serverThread, user));
                    serverThread.sendResponse(new Response(Rsp.OK));
                    System.out.printf("User %s logged in%n", user.getName());
                    break;
                case LIST:
                    serverThread.sendResponse(new ResponseAuctionList(serverThread.getAuctionList()));
                    break;
                case END:
                    serverThread.setQuit();
                    break;
                default:
                    System.err.printf("Invalid command %s in connected state%n", command);
            }
        }

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
                    System.err.printf("Invalid command %s in registered state%n", command);
            }
        }

        private void logout() {
            UserList userList = serverThread.getUserList();
            if (!userList.logout(user)) {
                serverThread.sendResponse(new Response(Rsp.ERROR));
                System.out.printf("User %s logout failed: not logged in%n", user.getName());
                return;
            }
            serverThread.setState(new StateConnected(serverThread));
            serverThread.sendResponse(new Response(Rsp.OK));
            System.out.printf("User %s logged out%n", user.getName());
        }

    }

}
