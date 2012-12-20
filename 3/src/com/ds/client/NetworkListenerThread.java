
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import com.ds.client.Parcel.Type;
import com.ds.loggers.Log;

public class NetworkListenerThread implements Runnable {

    private static final long RETRY_MS = 5000;

    private final ParsedArgs args;
    private final Data data;
    private final BlockingQueue<Parcel> q;

    public NetworkListenerThread(ParsedArgs args, Data data) throws IOException {
        this.args = args;
        this.data = data;
        this.q = data.getProcessorQueue();
    }

    @Override
    public void run() {
        BufferedReader in;
        try {
            in = establishConnection();
        } catch (InterruptedException e) {
            Log.i("NetworkListenerThread interrupted");
            return;
        }

        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                q.add(new StringParcel(Type.PARCEL_NETWORK, msg));
            }
        } catch (Throwable t) {
            Log.e(t.getMessage());
        } finally {
            q.add(new Parcel(Type.PARCEL_CONNECTION_LOST));
            Log.i("NetworkListenerThread terminating");
        }
    }

    /**
     * Attempts to establish a connection. If unsuccessful, waits for RETRY_MS and then tries again
     * until a connection is established or it is interrupted.
     * New connections are put on the processor queue.
     */
    private BufferedReader establishConnection() throws InterruptedException {
        BufferedReader in = null;

        boolean failed;
        do {
            Socket sock = null;
            try {
                failed = false;

                sock = new Socket(args.getHost(), args.getTcpPort());
                data.addSocket(sock);

                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                q.add(new SocketParcel(Type.PARCEL_CONNECTION_ESTABLISHED, sock));
            } catch (UnknownHostException e) {
                failed = true;
                if (in != null) { try { in.close(); in = null; } catch (IOException e1) { } }
                if (sock != null) { try { sock.close(); sock = null; } catch (IOException e1) { } }
                data.removeSocket(sock);
            } catch (IOException e) {
                failed = true;
                if (in != null) { try { in.close(); in = null; } catch (IOException e1) { } }
                if (sock != null) { try { sock.close(); sock = null; } catch (IOException e1) { } }
                data.removeSocket(sock);
            }

            if (failed) {
                Thread.sleep(RETRY_MS);
            }
        } while (failed);

        return in;
    }

}
