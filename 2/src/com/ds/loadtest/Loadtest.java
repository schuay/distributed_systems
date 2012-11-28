package com.ds.loadtest;

import com.ds.loggers.Log;
import com.ds.management.AnalyticsSubscriber;
import com.ds.util.LoadTestProperties;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;

public class Loadtest {
    private static final LinkedList<LoadtestClient> clients =
        new LinkedList<LoadtestClient>();
    private static AnalyticsSubscriber analSub;

    public static void main(String[] args) {
        /* Handle arguments. */

        ParsedArgs parsedArgs;
        try {
            parsedArgs = new ParsedArgs(args);
            Log.i("Host: %s TCP Port: %d Bind Name: %s", parsedArgs.getHost(),
                    parsedArgs.getTcpPort(), parsedArgs.getBindName());
        } catch (IllegalArgumentException e) {
            System.err.printf("Usage: java %s <host> <tcpPort> <bindName>%n",
                    Loadtest.class.getName());
            return;
        }

        Socket sock = null;
        long millisStart = new Date().getTime();
        try {
            LoadTestProperties prop = new LoadTestProperties();

            analSub = new AnalyticsSubscriber(parsedArgs.getBindName());
            analSub.setMode(AnalyticsSubscriber.PrintMode.AUTO);
            analSub.subscribe(".*");

            for (int i = 0; i < prop.getClients(); i++) {
                sock = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());
                LoadtestClient client = new LoadtestClient(i, prop, sock, millisStart);
                clients.add(client);
                sock = null;
                client.start();
            }
        } catch (Exception e) {
            Log.e(e.getLocalizedMessage());
            if (sock != null) {
                try { sock.close(); } catch (IOException ie) {}
            }
            shutdown();
        }
    }

    private static void shutdown() {
        for (LoadtestClient client : clients) {
            client.shutdown();
        }

        if (analSub != null) {
            analSub.shutdown();
        }
    }

    /**
     * Parses command-line arguments.
     */
    private static class ParsedArgs {
        private final String host;

        private final int tcpPort;

        private final String bindName;

        public ParsedArgs(String[] args) {
            if (args.length != 3) {
                throw new IllegalArgumentException();
            }

            host = args[0];

            try {
                tcpPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException();
            }

            bindName = args[2];
        }

        public String getHost() {
            return host;
        }

        public int getTcpPort() {
            return tcpPort;
        }

        public String getBindName() {
            return bindName;
        }
    }
}
