
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
            System.out.printf("TCP Port: %d%n", parsedArgs.getTcpPort());
            System.out.printf("Analytics Binding Name: %s%n", parsedArgs.getAnalyticsBindingName());
            System.out.printf("Billing Binding Name: %s%n", parsedArgs.getBillingBindingName());
        } catch (IllegalArgumentException e) {
            System.err.printf("Usage: java %s <TCP Port> <Analytics Binding Name> <Billing Binding Name>%n",
                    Server.class.getName());
            return;
        }

        /* Open the server socket. */

        try {
            serverSocket = new ServerSocket(parsedArgs.getTcpPort());
        } catch (IOException e) {
            serverSocket.close();
            System.err.println(e.getMessage());
            return;
        }

        /* Begin listening for the user to trigger shutdown (by pressing 'Enter'). */

        Thread thread = new Thread(new Server());
        thread.start();

        System.out.println("Server started, press Enter to initiate shutdown.");

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
                System.err.println(e.getMessage());
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
        serverSocket.close();

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
            System.err.println(e.getMessage());
        }
    }

    /* Prevent other classes from creating a Server instance. */
    private Server() { }

    /**
     * Waits for the user to press Enter, and then triggers shutdown.
     */
    @Override
    public void run() {
        try {
            int in;
            do {
                in = System.in.read();
            } while (in != '\n');

            listening = false;
            serverSocket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Parses command-line arguments.
     */
    private static class ParsedArgs {
        private final int tcpPort;

        private final String analyticsBindingName;

        private final String billingBindingName;

        public ParsedArgs(String[] args) {
            if (args.length != 3) {
                throw new IllegalArgumentException();
            }

            try {
                tcpPort = Integer.parseInt(args[0]);
                analyticsBindingName = args[1];
                billingBindingName = args[2];
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
    }

    /**
     * Encapsulates all server data.
     */
    public static class Data {

        private final UserList userList = new UserList();
        private final AuctionList auctionList = new AuctionList();
        private final EventLogger eventLogger = new EventLogger();

        public Data() {
            auctionList.addOnEventListener(eventLogger);

            /* TODO: Add an EventForwarder for the analytics and billing servers. */
        }

        public UserList getUserList() {
            return userList;
        }

        public AuctionList getAuctionList() {
            return auctionList;
        }
    }
}
