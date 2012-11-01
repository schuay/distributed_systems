package com.ds.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

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

	private final List<String> notifications = new ArrayList<String>();
	private int port;
	private InetAddress address;

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
			Server.sendUdp(address, port, msg);
		}
		notifications.clear();
	}

	public void login(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		this.loggedIn = true;
		processNotifications();
	}

	public void logout() {
		this.loggedIn = false;
	}
}
