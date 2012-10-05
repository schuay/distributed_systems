package com.ds.server;

public class Server {

	public static void main(String[] args) {
		ParsedArgs parsedArgs = null;
		try {
			parsedArgs = new ParsedArgs(args);
			System.out.printf("TCP Port: %d%n", parsedArgs.getTcpPort());
		} catch (IllegalArgumentException e) {
			System.err.printf("Usage: java %s <tcpPort>%n", Server.class.getName());
			return;
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
