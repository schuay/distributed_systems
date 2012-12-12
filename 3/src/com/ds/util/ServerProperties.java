package com.ds.util;

import java.io.IOException;

public class ServerProperties extends SimpleProperties {

    public ServerProperties() throws IOException {
        super("server.properties");
    }

    public String getPassphrase() {
        return getProperty("server.passphrase");
    }
}
