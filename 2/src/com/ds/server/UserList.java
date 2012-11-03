
package com.ds.server;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A list of all users known to the system.
 * Users are kept in this list even if they log out.
 */
public class UserList {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

    /**
     * Logs in the specified user.
     * 
     * @return true The user if login was successful, null otherwise.
     */
    public synchronized User login(String userName) {
        if (!users.containsKey(userName)) {
            users.put(userName, new User(userName));
        }

        User user = users.get(userName);
        if (user.isLoggedIn()) {
            return null;
        }

        user.login();
        return user;
    }

    /**
     * Logs out the specified user.
     * 
     * @return true if logout was successful, false otherwise.
     */
    public synchronized boolean logout(User user) {
        if (!user.isLoggedIn()) {
            return false;
        }

        user.logout();
        return true;
    }


    /**
     * Represents a system user.
     */
    public static class User {

        public static final User NONE = new User("none");

        private final String name;
        public String getName() {
            return name;
        }

        private boolean loggedIn = false;
        public boolean isLoggedIn() {
            return loggedIn;
        }

        /* Prevent construction by all classes except UserList. */
        private User(String name) {
            this.name = name;
        }

        private void login() {
            this.loggedIn = true;
        }

        private void logout() {
            this.loggedIn = false;
        }
    }
}
