
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.ds.client.Parcel.Type;
import com.ds.loggers.Log;

public class Client {

    private static final String INDENT = "> ";

    private static Thread networkListenerThread;
    private static Thread processorThread;
    private static Thread p2pThread;

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

            networkListenerThread.start();
            processorThread.start();
            p2pThread.start();

            inputLoop(data);

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
                /* The JXSE thread is unable to shut itself down correctly. */
            } catch (InterruptedException e) {
                interrupted = true;
            }
        } while (interrupted);

        /* JXSE 2.6 does not manage to shut itself down correctly
         * (see http://www.java.net/node/706775). */
        System.exit(0);
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
