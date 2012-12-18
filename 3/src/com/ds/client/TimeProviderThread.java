
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ds.channels.Channel;
import com.ds.channels.NopChannel;
import com.ds.loggers.Log;

/**
 * Listens for incoming connections on a given server socket,
 * and processes timestamp requests as they appear.
 */
public class TimeProviderThread implements Runnable {

    private static final String TAG = TimeProviderThread.class.getName();

    private static final Pattern pattern =
            Pattern.compile("^!getTimestamp ([0-9]+) ([0-9]+)$");

    private final ServerSocket serverSocket;

    public TimeProviderThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {

        Socket s = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            while (true) {
                s = serverSocket.accept();

                /* TODO: Construct the channel from the currently logged in
                 * client's key.
                 */
                Channel channel = new NopChannel();

                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new PrintWriter(s.getOutputStream());

                handleTimestampRequest(in, out, channel);

                out.close(); out = null;
                in.close(); in = null;
                s.close(); s = null;
            }
        } catch (IOException e) {
            Log.e(e.getMessage());
        } finally {
            if (out != null) {       out.close(); }
            if (in  != null) { try {  in.close(); } catch (IOException e) { } }
            if (s   != null) { try {   s.close(); } catch (IOException e) { } }
        }
    }

    /**
     * Reads a single timestamp request from in and sends its response to out.
     * Any error (incorrect incoming syntax, no user logged in, etc) results
     * in an exception.
     * 
     * @param in
     * @param out
     * @param channel
     * @throws IOException
     */
    private void handleTimestampRequest(BufferedReader in, PrintWriter out,
            Channel channel) throws IOException {
        String request = in.readLine();
        if (request == null) {
            Log.w("Empty messsage received on %s", TAG);
            return;
        }

        request = new String(channel.decode(request.getBytes(Channel.CHARSET)), Channel.CHARSET);

        Matcher m = pattern.matcher(request);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }

        long time = System.currentTimeMillis();
        String response = String.format("!timestamp %s %s %d", m.group(1), m.group(2), time);

        response = new String(channel.encode(response.getBytes(Channel.CHARSET)), Channel.CHARSET);
        out.println(response);
    }

}
