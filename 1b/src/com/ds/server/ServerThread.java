package com.ds.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.ds.common.Command;

public class ServerThread implements Runnable {

	private final ObjectInputStream in;
	private final ObjectOutputStream out;
	private final int id;
	private final ServerData serverData;
	private State state = new StateConnected(this);
	private boolean quit = false;

	public ServerThread(int id, Socket socket, ServerData serverData) throws IOException {
		this.id = id;
		this.serverData = serverData;

		in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());

		System.out.printf("ServerThread %d created%n", id);
	}

	@Override
	public void run() {
		try {
			Command command;
			while (!quit && (command = (Command)in.readObject()) != null) {
				System.out.printf("Received command: %s%n", command);
				state.processCommand(command);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.printf("ServerThread %d shutting down%n", id);
	}

	public void setQuit() {
		quit = true;
	}

	public UserList getUserList() {
		return serverData.getUserList();
	}

	public AuctionList getAuctionList() {
		return serverData.getAuctionList();
	}

	public void setState(State state) {
		this.state = state;
	}

	public void sendResponse(Object object) {
		try {
			out.writeObject(object);
		} catch (IOException e) {
			setQuit();
			e.printStackTrace();
		}
	}
}
