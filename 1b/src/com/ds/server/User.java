package com.ds.server;

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
	}

	public User(String name) {
		this.name = name;
	}

}
