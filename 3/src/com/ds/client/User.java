package com.ds.client;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class User {
    private static final Pattern pattern =
            Pattern.compile("^([^:]+):([0-9]+) - ([a-zA-Z0-9_\\-]+)$");

    private InetSocketAddress addr;
    private String name;

    private User() { }

    public static User fromString(String s) {
        Matcher m = pattern.matcher(s);

        if (!m.matches()) {
            throw new IllegalArgumentException();
        }

        User user = new User();
        user.addr = new InetSocketAddress(m.group(1), Integer.parseInt(m.group(2)));
        user.name = m.group(3);

        return user;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }

    public String getName() {
        return name;
    }
}
