package com.ds.server;

import java.util.concurrent.ConcurrentHashMap;

public class UserListModel {

	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();

	public void addUser(User user) {
		users.put(user.getName(), user);
	}
}
