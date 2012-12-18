
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

            Log.i("Connection successful.");

            networkListenerThread = new Thread(new NetworkListenerThread(socket, data));
            processorThread = new Thread(new ProcessorThread(socket, data));
            timeProviderThread = new Thread(new TimeProviderThread(serverSocket));

            networkListenerThread.start();
            processorThread.start();
            timeProviderThread.start();

            inputLoop(data);

            socket.close();
            socket = null;

            serverSocket.close();
            serverSocket = null;

            networkListenerThread.join();
            processorThread.join();
            timeProviderThread.join();

        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
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

                data.getQueue().add(new Parcel(Type.PARCEL_TERMINAL, msg));

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
