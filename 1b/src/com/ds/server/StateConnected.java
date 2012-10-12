package com.ds.server;

import com.ds.common.Command;
import com.ds.common.CommandLogin;
import com.ds.common.Response;
import com.ds.common.Response.Rsp;

public class StateConnected implements State {

	private final ServerThread serverThread;

	public StateConnected(ServerThread serverThread) {
		this.serverThread = serverThread;
	}

	@Override
	public void processCommand(Command command) {
		switch (command.getId()) {
		case LOGIN:
			CommandLogin commandLogin = (CommandLogin)command;
			UserListModel userList = serverThread.getUserList();
			User user = new User(commandLogin.getUser());
			if (userList.contains(user)) {
				serverThread.sendResponse(new Response(Rsp.ERROR));
				System.out.printf("User %s login failed: already logged in%n", user.getName());
				return;
			}
			userList.add(user);
			serverThread.setState(new StateRegistered(serverThread, user));
			serverThread.sendResponse(new Response(Rsp.OK));
			System.out.printf("User %s logged in%n", user.getName());
			break;
		case LIST:
			break;
		case END:
			serverThread.setQuit();
			break;
		default:
			System.err.printf("Invalid command %s in connected state%n", command);
		}
	}

}
