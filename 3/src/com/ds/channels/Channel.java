package com.ds.channels;

import java.io.IOException;


public abstract class Channel {

    public static final String CHARSET = "UTF-8";

    /** Set if the message is encrypted. */
    public static final int FLAG_ENCRYPTED = 1 >> 0;

    /** Set if the message has been mangled (for example, if the HMAC doesn't match). */
    public static final int FLAG_MANGLED = 1 >> 2;


    protected int flags = 0;


    public abstract byte[] encode(byte[] in) throws IOException;

    public abstract byte[] decode(byte[] in) throws IOException;

    /**
     * Returns the flags regarding the last operation.
     * Flags are set by decode() and cleared by encode().
     */
    public final int getFlags() {
        return flags;
    }

}
