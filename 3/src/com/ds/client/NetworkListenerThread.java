
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
                data.getQueue().add(new Parcel(Type.PARCEL_NETWORK, msg));
            }
        } catch (Exception e) {
            Log.w("ResponseThread terminating");
        }

        //            while (true) {
        //
        //                /* This complicated construct is required to handle the case in which we must make
        //                 * sure that the next incoming message is received using a specific channel.
        //                 *
        //                 * While we are listening, we lock a semaphore. When the socket timeout occurs,
        //                 * we give the other thread a chance to switch the channel.
        //                 */
        //
        //                Response response = null;
        //                boolean timeout;
        //                do {
        //                    try {
        //                        timeout = false;
        //                        data.getSemaphore().acquire();
        //                        response = Response.parse(data.getChannel().readLine());
        //                    } catch (SocketTimeoutException e) {
        //                        timeout = true;
        //                    } finally {
        //                        data.getSemaphore().release();
        //                    }
        //                } while (timeout);
        //
        //                if (response == null) {
        //                    break;
        //                }
        //
        //                switch (response.getResponse()) {
        //                case OK:
        //
        //                    ResponseOk r = (ResponseOk)response;
        //                    CommandChallenge c = new CommandChallenge(r.getServerChallenge());
        //
        //                    /* TODO: Check if received client challenge equals sent challenge. */
        //
        //                    /* Set up the AES channel. */
        //
        //                    data.resetChannel();
        //
        //                    Channel b64c = new Base64Channel(data.getChannel());
        //                    Channel aesc = new AesChannel(b64c, r.getSecretKey(),
        //                            new IvParameterSpec(r.getIv()));
        //
        //                    data.setChannel(aesc);
        //                    aesc.write(c.toString().getBytes());
        //
        //                    break;
        //
        //                default:
        //                    System.out.println(response);
        //                    break;
        //                }
        //            }
    }

}
