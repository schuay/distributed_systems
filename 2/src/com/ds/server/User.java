package com.ds.server;

public class User {

    public static final User NONE = new User("none");

    private final String name;
    public String getName() {
        return name;
    }

    private boolean loggedIn = false;
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public User(String name) {
        this.name = name;
    }

    public void login() {
        this.loggedIn = true;
    }

    public void logout() {
        this.loggedIn = false;
    }
}
