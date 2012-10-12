package com.ds.client;

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