package com.ds.util;

import java.io.IOException;

public class UserProperties extends SimpleProperties {

    public UserProperties() throws IOException {
        super("user.properties");
    }

    public String getHash(String user) {
        return getProperty(user);
    }
}
