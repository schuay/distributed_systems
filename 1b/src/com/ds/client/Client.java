package com.ds.client;

public class Client {

	public static void main(String[] args) {
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
