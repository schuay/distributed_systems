package com.ds.channels;

import java.io.IOException;
import java.net.Socket;

import com.ds.util.SecurityUtils;

public class Base64Channel extends TcpChannel {

    public Base64Channel(Socket socket) throws IOException {
        super(socket);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        byte[] encoded = SecurityUtils.toBase64(bytes);
        super.write(encoded);
    }

    @Override
    public String readLine() throws IOException {
        return new String(read(), CHARSET);
    }

    @Override
    public byte[] read() throws IOException {
        String encoded = super.readLine();
        return SecurityUtils.fromBase64(encoded.getBytes());
    }
}
