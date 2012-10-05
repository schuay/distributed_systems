package com.ds.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server implements Runnable {

	private static volatile boolean listening = true;
	private static ServerSocket serverSocket = null;

	public static void main(String[] args) {
		ParsedArgs parsedArgs = null;
		try {
			parsedArgs = new ParsedArgs(args);
			System.out.printf("TCP Port: %d%n", parsedArgs.getTcpPort());
		} catch (IllegalArgumentException e) {
			System.err.printf("Usage: java %s <tcpPort>%n", Server.class.getName());
			return;
		}

		try {
			serverSocket = new ServerSocket(parsedArgs.getTcpPort());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		Thread thread = new Thread(new Server());
		thread.start();

		System.out.println("Server started, press Enter to initiate shutdown.");

		/* Initialization is done. We will not accept new connections until server shutdown
		 * is triggered.
		 */

		int id = 0;
		ExecutorService executorService = Executors.newCachedThreadPool();
		while (listening) {
			try {
				executorService.submit(new ServerThread(id++, serverSocket.accept()));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

		/* Shutdown gracefully. */

		try {
			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	/* Prevent other classes from creating a Server instance. */
	private Server() { }

	/**
	 * Waits for the user to press Enter, and then triggers shutdown.
	 */
	@Override
	public void run() {
		try {
			int in;
			do {
				in = System.in.read();
			} while (in != '\n');

			listening = false;
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
}

class ParsedArgs {
	private final int tcpPort;

	public ParsedArgs(String[] args) {
		if (args.length != 1) {
			throw new IllegalArgumentException();
		}

		try {
			tcpPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public int getTcpPort() {
		return tcpPort;
	}
}
