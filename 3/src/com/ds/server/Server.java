
package com.ds.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.ds.loggers.EventLogger;
import com.ds.loggers.Log;
import com.ds.util.Initialization;
import com.ds.util.Utils;

public class Server implements Runnable {

    private static volatile boolean listening = true;
    private static ServerSocket serverSocket = null;
    private static List<Socket> sockets = new ArrayList<Socket>();
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Data serverData = new Data();

    public static void main(String[] args) throws IOException {

        /* Handle command-line arguments. */

        ParsedArgs parsedArgs = null;
        try {
            parsedArgs = new ParsedArgs(args);
            Log.i("TCP Port: %d", parsedArgs.getTcpPort());
            Log.i("Analytics Binding Name: %s", parsedArgs.getAnalyticsBindingName());
            Log.i("Billing Binding Name: %s", parsedArgs.getBillingBindingName());
            Log.i("Server Key: %s", parsedArgs.getServerKey());
            Log.i("Client Key Directory: %s", parsedArgs.getClientKeyDir());
        } catch (IllegalArgumentException e) {
            System.err.printf("Usage: java %s <TCP Port> <Analytics Binding Name> <Billing Binding Name> <Server Key> <Client Key Directory>%n",
                    Server.class.getName());
            shutdown();
            return;
        }

        /* Connect event listeners. */

        try {
            Initialization.setSystemProperties();

            EventLogger eventLogger = new EventLogger();
            serverData.getAuctionList().addOnEventListener(eventLogger);
            serverData.getUserList().addOnEventListener(eventLogger);

            EventForwarder analyticsForwarder = new EventForwarder(parsedArgs.getAnalyticsBindingName());
            serverData.getAuctionList().addOnEventListener(analyticsForwarder);
            serverData.getUserList().addOnEventListener(analyticsForwarder);

            BillingForwarder billingForwarder = new BillingForwarder(parsedArgs.getBillingBindingName());
            serverData.getAuctionList().addBillingListener(billingForwarder);
        } catch (Throwable t) {
            Log.e(t.getMessage());
            shutdown();
            return;
        }

        /* Open the server socket. */

        try {
            serverSocket = new ServerSocket(parsedArgs.getTcpPort());
        } catch (IOException e) {
            Log.e(e.getMessage());
            shutdown();
            return;
        }

        /* Begin listening for the user to trigger shutdown (by entering '!exit'). */

        Thread thread = new Thread(new Server());
        thread.start();

        Utils.printRunningMsg("Auction server");

        /*
         * Initialization is done. We will now accept new connections until
         * server shutdown is triggered.
         */

        int id = 0;
        while (listening) {
            try {
                Socket socket = serverSocket.accept();
                sockets.add(socket);
                executorService.submit(new ServerThread(id++, socket, serverData));
            } catch (IOException e) {
                Log.e(e.getMessage());
            }
        }

        shutdown();
    }

    /*
     * Shutdown gracefully. Stop accepting new client communications;
     * Cancel timers to stop the timer thread; Close all open client
     * sockets to terminate their tasks; and wait for all tasks to
     * complete.
     */
    private static void shutdown() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }

        executorService.shutdownNow();

        for (Socket socket : sockets) {
            if (socket.isClosed()) {
                continue;
            }
            socket.close();
        }

        serverData.getAuctionList().cancelTimers();

        try {
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(e.getMessage());
        }
    }

    /* Prevent other classes from creating a Server instance. */
    private Server() { }

    /**
     * Waits for the user to enter the exit command, and then triggers shutdown.
     */
    @Override
    public void run() {
        Utils.waitForExit();

        listening = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
    }

    /**
     * Parses command-line arguments.
     */
    private static class ParsedArgs {

        private final int tcpPort;
        private final String analyticsBindingName;
        private final String billingBindingName;
        private final String serverKey;
        private final String clientKeyDir;

        public ParsedArgs(String[] args) {
            if (args.length != 5) {
                throw new IllegalArgumentException();
            }

            try {
                tcpPort = Integer.parseInt(args[0]);
                analyticsBindingName = args[1];
                billingBindingName = args[2];
                serverKey = args[3];
                clientKeyDir = args[4];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException();
            }
        }

        public int getTcpPort() {
            return tcpPort;
        }

        public String getAnalyticsBindingName() {
            return analyticsBindingName;
        }

        public String getBillingBindingName() {
            return billingBindingName;
        }

        public String getServerKey() {
            return serverKey;
        }

        public String getClientKeyDir() {
            return clientKeyDir;
        }
    }

    /**
     * Encapsulates all server data.
     */
    public static class Data {

        private final UserList userList = new UserList();
        private final AuctionList auctionList = new AuctionList();

        public UserList getUserList() {
            return userList;
        }

        public AuctionList getAuctionList() {
            return auctionList;
        }
    }
}
