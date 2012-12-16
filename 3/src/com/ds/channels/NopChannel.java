package com.ds.channels;

public class NopChannel implements Channel {

    @Override
    public byte[] encode(byte[] in) {
        return in;
    }

    @Override
    public byte[] decode(byte[] in) {
        return in;
    }

}
