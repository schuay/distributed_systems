
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.ds.common.Command;
import com.ds.common.Command.Cmd;
import com.ds.loggers.Log;

public class Client {

    private static final String INDENT = "> ";

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
        PrintWriter out = null;
        try {
            socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());

            Thread responseThread = new Thread(new ResponseThread(socket));
            responseThread.start();

            out = new PrintWriter(socket.getOutputStream());
            Log.i("Connection successful.");

            inputLoop(parsedArgs, out);

            responseThread.join();
        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        }
    }

    private static void inputLoop(ParsedArgs args, PrintWriter out) throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        /* Initial indentation. */

        System.out.print(INDENT);

        String userInput;
        Command command;
        while ((userInput = stdin.readLine()) != null) {

            /* Parse and send the command. */

            try {
                command = Command.parse(userInput);
                out.println(command.toString());
                out.flush();

                if (command.getId() == Cmd.END) {
                    break;
                }

                System.out.print(INDENT);
            } catch (IllegalArgumentException e) {
                Log.e("Invalid command ignored");
            }
        }

        stdin.close();
    }

    /**
     * Parses command-line arguments.
     */
    private static class ParsedArgs {

        private final String host;
        private final int tcpPort;
        private final int udpPort;
        private final String serverPublicKey;
        private final String clientKeyDir;


        public ParsedArgs(String[] args) {
            if (args.length != 5) {
                throw new IllegalArgumentException();
            }

            try {
                host = args[0];
                tcpPort = Integer.parseInt(args[1]);
                udpPort = Integer.parseInt(args[2]);
                serverPublicKey = args[3];
                clientKeyDir = args[4];
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException();
            }
        }

        public String getHost() {
            return host;
        }

        public int getTcpPort() {
            return tcpPort;
        }

        public int getUdpPort() {
            return udpPort;
        }

        public String getServerPublicKey() {
            return serverPublicKey;
        }

        public String getClientKeyDir() {
            return clientKeyDir;
        }
    }
}
