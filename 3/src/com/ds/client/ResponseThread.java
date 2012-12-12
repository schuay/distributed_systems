
package com.ds.client;

import java.io.IOException;

import com.ds.interfaces.StringChannel;
import com.ds.loggers.Log;
import com.ds.responses.Response;

public class ResponseThread implements Runnable {

    private final StringChannel channel;

    public ResponseThread(StringChannel channel) throws IOException {
        this.channel = channel;
    }

    @Override
    public void run() {
        try {
            Response response;
            while ((response = Response.parse(channel.readLine())) != null) {
                System.out.println(response);
            }
        } catch (Exception e) {
            Log.w("ResponseThread terminating");
        } finally {
            channel.close();
        }
    }

}
