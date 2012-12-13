package com.ds.channels;

import java.io.IOException;

public interface Channel {

    String CHARSET = "UTF-8";

    void write(byte[] bytes) throws IOException;

    String readLine() throws IOException;

    byte[] read() throws IOException;

    void close();
}
