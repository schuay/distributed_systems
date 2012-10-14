package com.ds.server;

import java.util.concurrent.ConcurrentHashMap;

public class UserList {

	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

	/**
	 * Logs in the specified user.
	 * @return true if login was successful, false otherwise.
	 */
	public synchronized boolean login(User user) {
		if (!users.containsKey(user.getName())) {
			users.put(user.getName(), user);
			return true;
		}

		user = users.get(user.getName());
		if (user.isLoggedIn()) {
			return false;
		}

		user.setLoggedIn(true);
		return true;
	}

	/**
	 * Logs out the specified user.
	 * @return true if logout was successful, false otherwise.
	 */
	public synchronized boolean logout(User user) {
		if (!users.containsKey(user.getName())) {
			return false;
		}

		user.setLoggedIn(false);
		return true;
	}
}
