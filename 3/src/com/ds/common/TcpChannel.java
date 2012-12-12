package com.ds.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.ds.interfaces.StringChannel;
import com.ds.loggers.Log;

public class TcpChannel implements StringChannel {

    private final BufferedReader in;
    private final PrintWriter out;
    private final Socket socket;

    public TcpChannel(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream());
    }

    @Override
    public void printf(String format, Object... args) {
        out.printf(format, args);
        out.println();
        out.flush();
    }

    @Override
    public String readLine() throws IOException {
        return in.readLine();
    }

    @Override
    public void close() {
        try {
            in.close();
        } catch (Throwable t) {
            Log.e(t.getMessage());
        }
        out.close();
        try {
            socket.close();
        } catch (Throwable t) {
            Log.e(t.getMessage());
        }
    }
}
