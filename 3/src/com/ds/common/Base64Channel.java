package com.ds.common;

import java.io.IOException;
import java.net.Socket;

import com.ds.util.SecurityUtils;

public class Base64Channel extends TcpChannel {

    public Base64Channel(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void printf(String format, Object... args) {
        byte[] from = String.format(format, args).getBytes();
        super.printf(SecurityUtils.toBase64(from).toString());
    }

    @Override
    public String readLine() throws IOException {
        byte[] from = super.readLine().getBytes();
        return SecurityUtils.fromBase64(from).toString();
    }
}
