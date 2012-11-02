
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
     * @return true if login was successful, false otherwise.
     */
    public synchronized boolean login(User user) {
        if (!users.containsKey(user.getName())) {
            users.put(user.getName(), user);
        }

        user = users.get(user.getName());
        if (user.isLoggedIn()) {
            return false;
        }

        user.login();
        return true;
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
