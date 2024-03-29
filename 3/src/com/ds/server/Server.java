
package com.ds.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.bouncycastle.openssl.PasswordFinder;

import com.ds.loggers.EventLogger;
import com.ds.loggers.Log;
import com.ds.util.Initialization;
import com.ds.util.SecurityUtils;
import com.ds.util.ServerProperties;

public class Server implements Runnable {

    private static final String CMD_EXIT = "!exit";
    private static final String CMD_STOP = "!stop";
    private static final String CMD_START = "!start";

    private static volatile boolean listening = true;
    private static ServerSocket serverSocket = null;
    private static List<Socket> sockets = new ArrayList<Socket>();
    private static ExecutorService executorService = null;
    private static Data serverData = null;
    private static Semaphore semaphore = new Semaphore(0);

    public static void main(String[] args) throws IOException {

        try {
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

            try {
                serverData = new Data(parsedArgs.getServerKey(), parsedArgs.getClientKeyDir());

                /* Connect event listeners. */

                Initialization.setSystemProperties();

                EventLogger eventLogger = new EventLogger();
                serverData.getAuctionList().addOnEventListener(eventLogger);
                serverData.getUserList().addOnEventListener(eventLogger);
            } catch (Throwable t) {
                Log.e(t.getMessage());
                shutdown();
                return;
            }

            /* Begin listening for the user to trigger shutdown (by entering '!exit'). */

            Thread thread = new Thread(new Server());
            thread.start();

            System.out.printf("Auction server started%n" +
                    "%s: initiate shutdown%n" +
                    "%s: close all sockets%n" +
                    "%s: start listening on the server socket%n",
                    CMD_EXIT, CMD_STOP, CMD_START);

            do {
                /* Open the server socket. */

                try {
                    serverSocket = new ServerSocket(parsedArgs.getTcpPort());
                    executorService = Executors.newCachedThreadPool();
                } catch (IOException e) {
                    Log.e(e.getMessage());
                    shutdown();
                    return;
                }

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
                    } catch (SocketException e) {
                        /* Closed server socket, probably by CMD_STOP. */
                        Log.i(e.getMessage());
                        break;
                    } catch (Throwable t) {
                        Log.e(t.getMessage());
                    }
                }

                shutdown();

                /* To prevent immediate reinitialization after a stop command,
                 * wait until acquiring the semaphore before going on. */
                boolean interrupted;
                do {
                    try {
                        interrupted = false;
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                } while (interrupted);

            } while (listening);
        } finally {
            /* Cancel timers to stop the timer thread. */

            if (serverData != null) {
                serverData.getAuctionList().cancelTimers();
            }
        }
    }

    /*
     * Shutdown gracefully. Stop accepting new client communications;
     * Close all open client
     * sockets to terminate their tasks; and wait for all tasks to
     * complete.
     */
    private static void shutdown() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }

        for (Socket socket : sockets) {
            if (socket.isClosed()) {
                continue;
            }
            socket.close();
        }
        sockets.clear();

        if (executorService != null) {
            try {
                executorService.shutdownNow();
                executorService.awaitTermination(5, TimeUnit.SECONDS);
                executorService = null;
            } catch (InterruptedException e) {
                Log.e(e.getMessage());
            }
        }
    }

    /* Prevent other classes from creating a Server instance. */
    private Server() { }

    /**
     * Waits for the user to enter the exit command, and then triggers shutdown.
     */
    @Override
    public void run() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(isr);

        String state = CMD_START;
        String line = null;
        do {
            try {
                line = in.readLine();

                if (CMD_START.equals(line) && !CMD_START.equals(state)) {
                    semaphore.release();
                    state = line;
                    Log.i("Server started");
                } else if (CMD_STOP.equals(line) && !CMD_STOP.equals(state)) {
                    serverSocket.close();
                    state = line;
                    Log.i("Server stopped");
                }
            } catch (IOException e) {
                Log.e(e.getMessage());
            }
        } while (!CMD_EXIT.equals(line));

        try { in.close(); } catch (IOException e) {}
        try { isr.close(); } catch (IOException e) {}

        listening = false;
        semaphore.release();

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

        private static final String PUBLIC_KEY_EXTENSION = ".pub.pem";
        private static final String SECRET_KEY_EXTENSION = ".key";

        private final UserList userList = new UserList();
        private final AuctionList auctionList;
        private final GroupBidMonitor groupBidMonitor;
        private final PrivateKey serverKey;
        private final Map<String, PublicKey> clientPublicKeys;
        private final Map<String, SecretKey> clientSecretKeys;

        public Data(String serverKey, String clientKeyDir) throws IOException {
            this.serverKey = readPrivateKey(serverKey);
            this.clientPublicKeys = Collections.unmodifiableMap(readClientPublicKeys(clientKeyDir));
            this.clientSecretKeys = Collections.unmodifiableMap(readClientSecretKeys(clientKeyDir));
            this.auctionList = new AuctionList();
            this.groupBidMonitor = new GroupBidMonitor(auctionList, userList);
        }

        private static PrivateKey readPrivateKey(String path) throws IOException {
            /* A passphrase finder that returns the phrase from server.properties. */

            PasswordFinder finder = new PasswordFinder() {
                @Override
                public char[] getPassword() {
                    try {
                        return new ServerProperties().getPassphrase().toCharArray();
                    } catch (IOException e) {
                        Log.e("Could not read server properties");
                        return null;
                    }
                }
            };

            return SecurityUtils.readPrivateKey(path, finder);
        }

        private static Map<String, PublicKey> readClientPublicKeys(String path) {
            Map<String, PublicKey> map = new HashMap<String, PublicKey>();

            File directory = new File(path);
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException("Invalid client key directory");
            }

            /* A filter for public keys. */

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(PUBLIC_KEY_EXTENSION));
                }
            };

            for (File file : directory.listFiles(filter)) {
                try {
                    String client = clientName(file.getName(), PUBLIC_KEY_EXTENSION);
                    PublicKey pub = SecurityUtils.readPublicKey(file.getAbsolutePath());
                    map.put(client, pub);
                    Log.d("Found keyfile: %s for client %s", file.getName(), client);
                } catch (Throwable t) {
                    Log.e("Error reading client keys: %s", t.getMessage());
                }
            }

            return map;
        }

        private static Map<String, SecretKey> readClientSecretKeys(String path) {
            Map<String, SecretKey> map = new HashMap<String, SecretKey>();

            File directory = new File(path);
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException("Invalid client key directory");
            }

            /* A filter for keys. */

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(SECRET_KEY_EXTENSION));
                }
            };

            for (File file : directory.listFiles(filter)) {
                try {
                    String client = clientName(file.getName(), SECRET_KEY_EXTENSION);
                    SecretKey pub = SecurityUtils.readSecretKey(file.getAbsolutePath(),
                            SecurityUtils.AES);
                    map.put(client, pub);
                    Log.d("Found keyfile: %s for client %s", file.getName(), client);
                } catch (Throwable t) {
                    Log.e("Error reading client keys: %s", t.getMessage());
                }
            }

            return map;
        }

        private static String clientName(String name, String extension) {
            return name.substring(0, name.length() - extension.length());
        }

        public UserList getUserList() {
            return userList;
        }

        public AuctionList getAuctionList() {
            return auctionList;
        }

        public GroupBidMonitor getGroupBidMonitor() {
            return groupBidMonitor;
        }

        public PrivateKey getServerKey() {
            return serverKey;
        }

        public PublicKey getClientPublicKey(String client) {
            return clientPublicKeys.get(client);
        }

        public SecretKey getClientSecretKey(String client) {
            return clientSecretKeys.get(client);
        }
    }
}
