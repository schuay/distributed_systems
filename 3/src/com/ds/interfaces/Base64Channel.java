package com.ds.interfaces;

import java.io.IOException;

public interface Base64Channel {
    void printBytes(byte[] bytes);
    byte[] readBytes() throws IOException;
}
