
package com.ds.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.ds.event.Event;
import com.ds.event.UserEvent;
import com.ds.loggers.EventListener;

/**
 * A list of all users known to the system.
 * Users are kept in this list even if they log out.
 */
public class UserList {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
    private final List<EventListener> listeners = new ArrayList<EventListener>();

    private final Set<User> blockedUsers = new HashSet<User>();
    private final Set<User> loggedInUsers = new HashSet<User>();

    /**
     * Logs in the specified user.
     * @param address
     * @param port
     * 
     * @return true The user if login was successful, null otherwise.
     */
    public synchronized User login(String userName, int port, InetAddress address) {
        if (!users.containsKey(userName)) {
            users.put(userName, new User(userName));
        }

        User user = users.get(userName);
        if (user.isLoggedIn()) {
            return null;
        }

        notifyListeners(new UserEvent(UserEvent.USER_LOGIN, user.getName()));
        user.login(address, port);
        loggedInUsers.add(user);
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
        loggedInUsers.remove(user);
        unblockUser(user);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (User user : users.values()) {
            if (!user.isLoggedIn()) {
                continue;
            }

            sb.append(String.format("%s%n", user.toString()));
        }
        return sb.toString();
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

    public synchronized int numLoggedInUsers() {
        return loggedInUsers.size();
    }

    public synchronized int numBlockedUsers() {
        return blockedUsers.size();
    }

    public synchronized void blockUser(User user) {
        blockedUsers.add(user);
    }

    public synchronized void unblockUser(User user) {
        blockedUsers.remove(user);
    }

    public synchronized int getMaxAcceptedConfirms(long current) {
        int max = 0;

        for (User u : users.values()) {
            int maybeMax = u.getAcceptedConfirms(current);
            if (maybeMax > max) {
                max = maybeMax;
            }
        }

        return max;
    }

    /**
     * Represents a system user.
     */
    public static class User {

        public static final User NONE = new User("none");

        private final List<Long> acceptedConfirms = new LinkedList<Long>();

        private int recomputeAcceptedConfirms(long current) {
            Iterator<Long> i = acceptedConfirms.iterator();
            while (i.hasNext()) {
                if (i.next() >= (current - GroupBidMonitor.FAIRNESS_TIME)) {
                    break;
                }

                i.remove();
            }

            return acceptedConfirms.size();
        }

        public synchronized void confirmAccepted(long current) {
            acceptedConfirms.add(current);
        }

        public synchronized int getAcceptedConfirms(long current) {
            recomputeAcceptedConfirms(current);
            return acceptedConfirms.size();
        }

        private final String name;
        public String getName() {
            return name;
        }

        private boolean loggedIn = false;
        private InetAddress address = null;
        private int port = 0;

        public boolean isLoggedIn() {
            return loggedIn;
        }

        /* Prevent construction by all classes except UserList. */
        private User(String name) {
            this.name = name;
        }

        private void login(InetAddress address, int port) {
            this.loggedIn = true;
            this.address = address;
            this.port = port;
        }

        private void logout() {
            this.loggedIn = false;
        }

        @Override
        public String toString() {
            return String.format("%s:%d - %s", address.toString(), port, name);
        }
    }
}
