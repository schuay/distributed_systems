package com.ds.util;

import java.io.IOException;

public class RegistryProperties extends SimpleProperties {

    public RegistryProperties() throws IOException {
        super("registry.properties");
    }

    public String getHost() {
        return getProperty("registry.host");
    }

    public int getPort() {
        return Integer.parseInt(getProperty("registry.port"));
    }
}
