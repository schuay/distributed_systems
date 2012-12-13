package com.ds.channels;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.ds.loggers.Log;

public class TcpChannel implements Channel {

    private final BufferedReader in;
    private final BufferedWriter out;
    private final Socket socket;

    public TcpChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        char[] chars = new String(bytes, CHARSET).toCharArray();

        out.write(chars);
        out.append('\n');
        out.flush();
    }

    @Override
    public String readLine() throws IOException {
        return in.readLine();
    }

    @Override
    public byte[] read() throws IOException {
        return in.readLine().getBytes();
    }

    @Override
    public void close() {

        /* TODO: Cleanup methods are not yet cleared up.
         * On the one hand, it makes sense for close() to call
         * close() on enclosed channels to clean up things like ciphers; on the
         * other, we don't want our underlying TcpChannel to be closed before we are
         * completely done.
         */

        try {
            in.close();
            out.close();
            socket.close();
        } catch (Throwable t) {
            Log.e(t.getMessage());
        }
    }
}
