package com.ds.common;

import java.util.StringTokenizer;

import com.ds.client.ParsedArgs;

public class CommandLogin extends Command {

	private static final long serialVersionUID = 8266776473396356465L;

	private final String user;
	public String getUser() {
		return user;
	}

	private final int udpPort;
	public int getUdpPort() {
		return udpPort;
	}

	protected CommandLogin(StringTokenizer st, ParsedArgs args) {
		super(Cmd.LOGIN);

		if (st.countTokens() < 1) {
			throw new IllegalArgumentException();
		}

		this.user = st.nextToken();
		this.udpPort = args.getUdpPort();
	}
}