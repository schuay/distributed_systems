package com.ds.channels;

import java.io.IOException;


public interface Channel {

    byte[] encode(byte[] in) throws IOException;
    byte[] decode(byte[] in) throws IOException;

}
