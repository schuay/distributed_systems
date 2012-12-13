
package com.ds.client;

import com.ds.client.Client.Data;
import com.ds.loggers.Log;
import com.ds.responses.Response;

public class ResponseThread implements Runnable {

    private final Data data;

    public ResponseThread(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        try {
            Response response;
            while ((response = Response.parse(data.getChannel().readLine())) != null) {
                System.out.println(response);
            }
        } catch (Exception e) {
            Log.w("ResponseThread terminating");
        }
    }

}
