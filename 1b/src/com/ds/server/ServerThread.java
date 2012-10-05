package com.ds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.ds.Command;

public class ServerThread implements Runnable {

	private final Socket socket;
	private final int id;

	public ServerThread(int id, Socket socket) {
		this.id = id;
		this.socket = socket;
		System.out.printf("ServerThread %d created%n", id);
	}

	@Override
	public void run() {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String userInput;

			boolean quit = false;
			Command command;
			while (!quit && (userInput = in.readLine()) != null) {
				try {
					command = Command.parse(userInput);
				} catch (IllegalArgumentException e) {
					System.err.printf("Invalid command '%s'%n", userInput);
					continue;
				}

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

			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
