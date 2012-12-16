package com.ds.channels;

import java.io.IOException;


public interface Channel {

    String CHARSET = "UTF-8";

    byte[] encode(byte[] in) throws IOException;
    byte[] decode(byte[] in) throws IOException;

}
