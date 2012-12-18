
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.ds.client.Parcel.Type;
import com.ds.loggers.Log;

public class NetworkListenerThread implements Runnable {

    private final Data data;
    private final BufferedReader in;

    public NetworkListenerThread(Socket socket, Data data) throws IOException {
        this.data = data;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                data.getProcessorQueue().add(new Parcel(Type.PARCEL_NETWORK, msg));
            }
        } catch (Exception e) {
            Log.w("ResponseThread terminating");
        }
    }

}
