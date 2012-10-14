package com.ds.server;

import java.util.Calendar;
import java.util.Date;

import com.ds.common.Command;
import com.ds.common.CommandCreate;
import com.ds.common.Response;
import com.ds.common.Response.Rsp;
import com.ds.common.ResponseAuctionList;

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
		case LIST:
			serverThread.sendResponse(new ResponseAuctionList(serverThread.getAuctionList()));
			break;
		case LOGOUT:
			logout();
			break;
		case CREATE:
			CommandCreate commandCreate = (CommandCreate)command;
			AuctionList auctionList = serverThread.getAuctionList();

			Calendar now = Calendar.getInstance();
			now.setTime(new Date());
			now.add(Calendar.SECOND, commandCreate.getDuration());

			int id = auctionList.add(commandCreate.getDescription(), user, now.getTime());
			serverThread.sendResponse(new ResponseAuctionCreated(id));
			break;
		case BID:
			break;
		case END:
			logout();
			serverThread.setQuit();
			break;
		default:
			System.err.printf("Invalid command %s in registered state%n", command);
		}
	}

	private void logout() {
		UserList userList = serverThread.getUserList();
		if (!userList.contains(user)) {
			serverThread.sendResponse(new Response(Rsp.ERROR));
			System.out.printf("User %s logout failed: not logged in%n", user.getName());
			return;
		}
		userList.remove(user);
		serverThread.setState(new StateConnected(serverThread));
		serverThread.sendResponse(new Response(Rsp.OK));
		System.out.printf("User %s logged out%n", user.getName());
	}

}
