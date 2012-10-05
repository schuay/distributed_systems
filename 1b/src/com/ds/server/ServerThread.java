package com.ds.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

			System.out.printf("ServerThread %d received '%s'%n", id, in.readLine());
			out.println("Hello World");

			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
