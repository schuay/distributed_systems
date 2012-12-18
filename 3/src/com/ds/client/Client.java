
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import com.ds.client.Parcel.Type;
import com.ds.loggers.Log;

public class Client {

    private static final String INDENT = "> ";

    private static Thread networkListenerThread;
    private static Thread processorThread;
    private static Thread timeProviderThread;
    private static Thread timeRetrieverThread;

    public static void main(String[] args) throws IOException {

        /* Handle arguments. */

        ParsedArgs parsedArgs;
        try {
            parsedArgs = new ParsedArgs(args);
            Log.i("Host: %s", parsedArgs.getHost());
            Log.i("TCP Port: %d", parsedArgs.getTcpPort());
            Log.i("UDP Port: %d", parsedArgs.getUdpPort());
            Log.i("Server Public Key: %s", parsedArgs.getServerPublicKey());
            Log.i("Client Key Directory: %s", parsedArgs.getClientKeyDir());
        } catch (IllegalArgumentException e) {
            System.err.printf("Usage: java %s <host> <tcpPort> <udpPort> <serverPublicKey> <clientKeyDir>%n",
                    Client.class.getName());
            return;
        }

        /* Create the socket, the response handler thread, and run the main loop.
         * Finally, clean up after ourself once the main loop has terminated or an error occurs.
         */

        ServerSocket serverSocket = null;
        Socket socket = null;
        Data data = null;
        try {
            serverSocket = new ServerSocket(parsedArgs.getUdpPort());
            socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());

            data  = new Data(parsedArgs);
            data.addSocket(socket);

            Log.i("Connection successful.");

            networkListenerThread = new Thread(new NetworkListenerThread(socket, data));
            processorThread = new Thread(new ProcessorThread(socket, data));
            timeProviderThread = new Thread(new TimeProviderThread(serverSocket));
            timeRetrieverThread = new Thread(new TimeRetrieverThread(data));

            networkListenerThread.start();
            processorThread.start();
            timeProviderThread.start();
            timeRetrieverThread.start();

            inputLoop(data);

            /* Main execution ends here. */

        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {

            /* Trigger thread termination by either interrupting it (if possible)
             * or shutting down its sockets. */

            if (timeRetrieverThread != null) {
                timeRetrieverThread.interrupt();
            }

            if (serverSocket != null) {
                serverSocket.close();
            }

            if (data != null) {
                for (Socket s : data.getSockets()) {
                    try { s.close(); } catch (Throwable t) { Log.e(t.getMessage()); }
                }
            }
        }

        boolean interrupted;
        do {
            try {
                interrupted = false;
                if (networkListenerThread != null) { networkListenerThread.join(); networkListenerThread = null; }
                if (processorThread != null) { processorThread.join(); processorThread = null; }
                if (timeProviderThread != null) { timeProviderThread.join(); timeProviderThread = null; }
                if (timeRetrieverThread != null) { timeRetrieverThread.join(); timeRetrieverThread = null; }
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

    }

    private static void inputLoop(Data data) throws IOException {
        BufferedReader stdin = null;

        try {
            stdin = new BufferedReader(new InputStreamReader(System.in));

            /* Initial indentation. */

            System.out.print(INDENT);

            String msg;
            while ((msg = stdin.readLine()) != null) {

                /* Parse and send the command. */

                data.getProcessorQueue().add(new Parcel(Type.PARCEL_TERMINAL, msg));

                if (msg.trim().equals("!end")) {
                    break;
                }

                System.out.print(INDENT);
            }
        } finally {
            if (stdin != null) {
                stdin.close();
            }
        }
    }
}
