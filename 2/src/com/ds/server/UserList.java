
package com.ds.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.ds.event.Event;
import com.ds.event.UserEvent;
import com.ds.interfaces.EventListener;

/**
 * A list of all users known to the system.
 * Users are kept in this list even if they log out.
 */
public class UserList {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
    private final List<EventListener> listeners = new ArrayList<EventListener>();

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

        notifyListeners(new UserEvent(UserEvent.USER_LOGIN, user.getName()));
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

        notifyListeners(new UserEvent(UserEvent.USER_LOGOUT, user.getName()));
        notifyListeners(new UserEvent(UserEvent.USER_DISCONNECTED, user.getName()));
        user.logout();
        return true;
    }

    public void addOnEventListener(EventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeOnEventListener(EventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(Event event) {
        synchronized (listeners) {
            for (EventListener listener : listeners) {
                listener.onEvent(event);
            }
        }
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
