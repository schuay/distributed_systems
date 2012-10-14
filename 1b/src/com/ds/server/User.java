package com.ds.server;

import java.util.ArrayList;
import java.util.List;

public class User {

	private final String name;
	public String getName() {
		return name;
	}

	private boolean loggedIn = true;
	public boolean isLoggedIn() {
		return loggedIn;
	}
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
		if (loggedIn) {
			processNotifications();
		}
	}

	private final List<String> notifications = new ArrayList<>();

	public User(String name) {
		this.name = name;
	}

	public void postNotification(String message) {
		notifications.add(message);
		if (loggedIn) {
			processNotifications();
		}
	}

	private void processNotifications() {
		for (String msg : notifications) {

		}
		notifications.clear();
	}

}
