
package com.ds.client;

import java.io.IOException;
import java.net.Socket;

import com.ds.loggers.Log;

public class Client {

    private static Thread networkListenerThread;
    private static Thread processorThread;
    private static Thread p2pThread;
    private static Thread terminalListenerThread;

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

        Socket socket = null;
        Data data = null;
        try {
            socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());

            data  = new Data(parsedArgs);
            data.addSocket(socket);

            Log.i("Connection successful.");

            networkListenerThread = new Thread(new NetworkListenerThread(socket, data));
            processorThread = new Thread(new ProcessorThread(socket, data));
            p2pThread = new Thread(new P2PThread(data));
            terminalListenerThread = new Thread(new TerminalListenerThread(data));

            networkListenerThread.start();
            processorThread.start();
            p2pThread.start();
            terminalListenerThread.start();

            terminalListenerThread.join();

            /* Main execution ends here. */

        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {

            /* Trigger thread termination by either interrupting it (if possible)
             * or shutting down its sockets. */

            if (p2pThread != null) {
                p2pThread.interrupt();
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
                if (terminalListenerThread != null) { terminalListenerThread.join(); terminalListenerThread = null; }
                /* The JXSE thread is unable to shut itself down correctly. */
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        /* JXSE 2.6 does not manage to shut itself down correctly
         * (see http://www.java.net/node/706775). */
        System.exit(0);
    }
}
