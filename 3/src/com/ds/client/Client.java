
package com.ds.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

import com.ds.channels.Channel;
import com.ds.channels.TcpChannel;
import com.ds.commands.Command;
import com.ds.commands.Command.Cmd;
import com.ds.commands.CommandLogin;
import com.ds.loggers.Log;
import com.ds.util.SecurityUtils;

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
        Channel channel = null;
        Data data = null;
        try {
            socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());
            channel = new TcpChannel(socket);
            data  = new Data(channel, parsedArgs);

            Thread responseThread = new Thread(new ResponseThread(data));
            responseThread.start();

            Log.i("Connection successful.");

            inputLoop(data);

            responseThread.join();
        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            if (data.getChannel() != null) {
                data.getChannel().close();
            }

            if (channel != null) {
                channel.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private static void inputLoop(Data data) throws IOException {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        /* Initial indentation. */

        System.out.print(INDENT);

        String userInput;
        Command command;
        while ((userInput = stdin.readLine()) != null) {

            /* Parse and send the command. */

            try {
                command = Command.parse(userInput);
                processCommand(data, command);

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

    private static void processCommand(Data data, Command command) throws IOException {
        switch (command.getId()) {
        case LOGIN:
            try {
                CommandLogin c = (CommandLogin)command;

                /* TODO: Keep this somewhere to handle the server challenge when it arrives. */

                PrivateKey key = readPrivateKey(data.getClientKeyDir(), c.getUser());

                /* Encrypt the login command with the server's public key. */

                Cipher cipher = SecurityUtils.getCipher(
                        SecurityUtils.RSA,
                        Cipher.ENCRYPT_MODE,
                        data.getServerKey(),
                        null);
                byte[] secretMessage = cipher.doFinal(c.toString().getBytes());

                /* TODO: Do the base64 encoding transparently in the channel. */

                byte[] encodedMessage = SecurityUtils.toBase64(secretMessage);
                data.getChannel().write(encodedMessage);
            } catch (Throwable t) {
                Log.e(t.getMessage());
            }
            break;
        default:
            data.getChannel().write(command.toString().getBytes());
            break;
        }
    }

    private static PrivateKey readPrivateKey(String clientKeyDir, String user) throws IOException {
        File dir = new File(clientKeyDir);
        File file = new File(dir, String.format("%s.pem", user));

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Could not read private key");
        }

        return SecurityUtils.readPrivateKey(file.getAbsolutePath());
    }

    protected static class Data {

        private Channel channel;
        private final PublicKey serverKey;
        private final String clientKeyDir;

        public Data(Channel channel, ParsedArgs args) throws IOException {
            this.setChannel(channel);
            this.serverKey = SecurityUtils.readPublicKey(args.getServerPublicKey());
            this.clientKeyDir = args.getClientKeyDir();
        }

        public synchronized void setChannel(Channel channel) {
            this.channel = channel;
        }

        public synchronized Channel getChannel() {
            return channel;
        }

        public PublicKey getServerKey() {
            return serverKey;
        }

        public String getClientKeyDir() {
            return clientKeyDir;
        }
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
