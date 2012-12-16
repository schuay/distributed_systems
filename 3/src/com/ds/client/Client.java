
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

            Log.i("Connection successful.");

            networkListenerThread = new Thread(new NetworkListenerThread(socket, data));
            processorThread = new Thread(new ProcessorThread(socket, data));

            networkListenerThread.start();
            processorThread.start();

            inputLoop(data);

            socket.close();
            socket = null;

            networkListenerThread.join();
            processorThread.join();

        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
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

    //    private static void processCommand(Data data, Command command) throws IOException {
    //        switch (command.getId()) {
    //        case LOGIN:
    //            try {
    //                CommandLogin c = (CommandLogin)command;
    //
    //                /* Encrypt the login command with the server's public key. */
    //
    //                data.resetChannel();
    //
    //                PrivateKey key = readPrivateKey(data.getClientKeyDir(), c.getUser());
    //                Channel b64c = new Base64Channel(data.getChannel());
    //                Channel rsac = new RsaChannel(b64c, data.getServerKey(), key);
    //
    //                /* Ensure that the response thread is listening on the correct
    //                 * channel before sending the command.
    //                 */
    //
    //                data.getSemaphore().acquire();
    //                data.setChannel(rsac);
    //                data.getSemaphore().release();
    //
    //                rsac.write(c.toString().getBytes());
    //
    //            } catch (Throwable t) {
    //                Log.e(t.getMessage());
    //            }
    //            break;
    //        case LOGOUT:
    //            data.getChannel().write(command.toString().getBytes());
    //            data.resetChannel();
    //            break;
    //        default:
    //            data.getChannel().write(command.toString().getBytes());
    //            break;
    //        }
    //    }

    //    private static PrivateKey readPrivateKey(String clientKeyDir, String user) throws IOException {
    //        File dir = new File(clientKeyDir);
    //        File file = new File(dir, String.format("%s.pem", user));
    //
    //        if (!file.exists() || !file.isFile()) {
    //            throw new IllegalArgumentException("Could not read private key");
    //        }
    //
    //        return SecurityUtils.readPrivateKey(file.getAbsolutePath());
    //    }
}
