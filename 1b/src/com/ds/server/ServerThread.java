package com.ds.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.ds.common.Command;

public class ServerThread implements Runnable {

	private final Socket socket;
	private final int id;
	private final ServerData serverData;

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

			boolean quit = false;
			Command command;
			while (!quit && (command = (Command)in.readObject()) != null) {
				System.out.printf("Received command: %s%n", command);

				switch (command.getId()) {
				case LOGIN:
				case LOGOUT:
				case LIST:
				case CREATE:
				case BID:
					break;
				case END:
					quit = true;
					break;
				}
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

}
