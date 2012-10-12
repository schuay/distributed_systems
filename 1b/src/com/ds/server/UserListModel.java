package com.ds.server;

import java.util.concurrent.ConcurrentHashMap;

public class UserListModel {

	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

	/* TODO: atomic check + add, check + remove operations. */
	public void add(User user) {
		users.put(user.getName(), user);
	}

	public boolean contains(User user) {
		return users.containsKey(user.getName());
	}

	public void remove(User user) {
		users.remove(user.getName());
	}
}
