package com.ds.server;

import com.ds.common.Command;

public class StateRegistered implements State {

	private final ServerThread serverThread;
	private final User user;

	public StateRegistered(ServerThread serverThread, User user) {
		this.serverThread = serverThread;
		this.user = user;
	}

	@Override
	public void processCommand(Command command) {
		switch (command.getId()) {
		case LOGOUT:
			UserListModel userList = serverThread.getUserList();
			if (!userList.contains(user)) {
				System.out.printf("User %s logout failed: not logged in%n", user.getName());
				return;
			}
			userList.remove(user);
			serverThread.setState(new StateConnected(serverThread));
			System.out.printf("User %s logged out%n", user.getName());
			break;
		case CREATE:
			break;
		case BID:
			break;
		case END:
			serverThread.setQuit();
			break;
		default:
			System.err.printf("Invalid command %s in registered state%n", command);
		}
	}

}
