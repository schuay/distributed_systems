package com.ds.channels;

import java.io.IOException;

public interface Channel {

    void write(byte[] bytes) throws IOException;
    String readLine() throws IOException;

    void close();
}
