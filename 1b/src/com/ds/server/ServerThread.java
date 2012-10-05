package com.ds.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread implements Runnable {

	private final Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream());

			out.println("Hello World");

			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
