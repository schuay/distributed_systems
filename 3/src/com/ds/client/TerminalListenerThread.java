
package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ds.client.Parcel.Type;
import com.ds.loggers.Log;

public class TerminalListenerThread implements Runnable {

    private static final String INDENT = "> ";

    private final Data data;

    public TerminalListenerThread(Data data) throws IOException {
        this.data = data;
    }

    @Override
    public void run() {
        BufferedReader stdin = null;
        try {
            stdin = new BufferedReader(new InputStreamReader(System.in));

            /* Initial indentation. */

            System.out.print(INDENT);

            String msg;
            while ((msg = stdin.readLine()) != null) {

                /* Parse and send the command. */

                data.getProcessorQueue().add(new StringParcel(Type.PARCEL_TERMINAL, msg));

                if (msg.trim().equals("!end")) {
                    break;
                }

                System.out.print(INDENT);
            }
        } catch (IOException e) {
            Log.e(e.getMessage());
        } finally {
            if (stdin != null) { try { stdin.close(); } catch (Throwable t) { } }
        }
    }

}
