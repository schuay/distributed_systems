
package com.ds.client;

import java.net.SocketTimeoutException;

import javax.crypto.spec.IvParameterSpec;

import com.ds.channels.AesChannel;
import com.ds.channels.Base64Channel;
import com.ds.channels.Channel;
import com.ds.client.Client.Data;
import com.ds.commands.CommandChallenge;
import com.ds.loggers.Log;
import com.ds.responses.Response;
import com.ds.responses.ResponseOk;

public class ResponseThread implements Runnable {

    private final Data data;

    public ResponseThread(Data data) {
        this.data = data;
    }

    @Override
    public void run() {
        try {
            while (true) {

                /* This complicated construct is required to handle the case in which we must make
                 * sure that the next incoming message is received using a specific channel.
                 *
                 * While we are listening, we lock a semaphore. When the socket timeout occurs,
                 * we give the other thread a chance to switch the channel.
                 */

                Response response = null;
                boolean timeout;
                do {
                    try {
                        timeout = false;
                        data.getSemaphore().acquire();
                        response = Response.parse(data.getChannel().readLine());
                    } catch (SocketTimeoutException e) {
                        timeout = true;
                    } finally {
                        data.getSemaphore().release();
                    }
                } while (timeout);

                if (response == null) {
                    break;
                }

                switch (response.getResponse()) {
                case OK:

                    ResponseOk r = (ResponseOk)response;
                    CommandChallenge c = new CommandChallenge(r.getServerChallenge());

                    /* TODO: Check if received client challenge equals sent challenge. */

                    /* Set up the AES channel. */

                    data.resetChannel();

                    Channel b64c = new Base64Channel(data.getChannel());
                    Channel aesc = new AesChannel(b64c, r.getSecretKey(),
                            new IvParameterSpec(r.getIv()));

                    data.setChannel(aesc);
                    aesc.write(c.toString().getBytes());

                    break;

                    /* TODO: Handle logouts and aborted handshakes. In these cases, the channel
                     * must be reset.
                     */

                default:
                    System.out.println(response);
                    break;
                }
            }
        } catch (Exception e) {
            Log.w("ResponseThread terminating");
        }
    }

}
