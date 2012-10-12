package com.ds.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.ds.common.Command;

public class ServerThread implements Runnable {

	private final Socket socket;
	private final int id;
	private final ServerData serverData;
	private State state = new StateConnected(this);
	private boolean quit = false;

	public ServerThread(int id, Socket socket, ServerData serverData) {
		this.id = id;
		this.socket = socket;
		this.serverData = serverData;

		System.out.printf("ServerThread %d created%n", id);
	}

	@Override
	public void run() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(socket.getInputStream());

			Command command;
			while (!quit && (command = (Command)in.readObject()) != null) {
				System.out.printf("Received command: %s%n", command);
				state.processCommand(command);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setQuit() {
		quit = true;
	}

	public UserListModel getUserList() {
		return serverData.getUserList();
	}

	public void setState(State state) {
		this.state = state;
	}
}
