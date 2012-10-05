package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private static PrintWriter out = null;
	private static BufferedReader in = null;

	private static final String INDENT = "> ";

	public static void main(String[] args) throws IOException {
		ParsedArgs parsedArgs = null;
		try {
			parsedArgs = new ParsedArgs(args);
			System.out.printf("Host: %s TCP Port: %d UDP Port: %d%n",
					parsedArgs.getHost(),
					parsedArgs.getTcpPort(),
					parsedArgs.getUdpPort());
		} catch (IllegalArgumentException e) {
			System.err.printf("Usage: java %s <host> <tcpPort> <udpPort>%n",
					Client.class.getName());
			return;
		}

		Socket socket = null;

		try {
			socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println(e.getMessage());
			return;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		System.out.println("Connection successful.");

		inputLoop();

		out.close();
		in.close();
		socket.close();
	}

	private static void inputLoop() throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String userInput;

		System.out.print(INDENT);

		while ((userInput = stdin.readLine()) != null) {
			out.println(userInput);
			System.out.print(INDENT);
		}

		stdin.close();
	}
}

class ParsedArgs {
	private final String host;
	private final int tcpPort;
	private final int udpPort;

	public ParsedArgs(String[] args) {
		if (args.length != 3) {
			throw new IllegalArgumentException();
		}

		host = args[0];

		try {
			tcpPort = Integer.parseInt(args[1]);
			udpPort = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException();
		}
	}

	public String getHost() {
		return host;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public int getUdpPort() {
		return udpPort;
	}
}
