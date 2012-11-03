
package com.ds.server;

import java.util.concurrent.ConcurrentHashMap;

/* The handling of users is unfortunate. First, methods of user are exposed
 * outside of UserList that shouldn't be exposed, and UserList methods take User arguments,
 * but then need to ignore them and look them up again by name to avoid altering incorrect instances.
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
        if (!users.containsKey(user.getName())) {
            return false;
        }

        users.get(user.getName()).logout();
        return true;
    }
}
