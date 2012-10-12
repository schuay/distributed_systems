package com.ds.common;

import java.util.StringTokenizer;

public class Command {
	public enum Cmd {
		LOGIN,
		LOGOUT,
		LIST,
		CREATE,
		BID,
		END
	}

	private final Cmd id;
	private final String command;

	public static Command parse(String line) {
		StringTokenizer st = new StringTokenizer(line);
		if (!st.hasMoreTokens()) {
			throw new IllegalArgumentException();
		}

		try {
			String token = st.nextToken().toLowerCase();
			if (token.equals("!logout")) {
				return new Command(Cmd.LOGOUT, line);
			} else if (token.equals("!list")) {
				return new Command(Cmd.LIST, line);
			} else if (token.equals("!end")) {
				return new Command(Cmd.END, line);
			} else if (token.equals("!login")) {
				return new CommandLogin(st, line);
			} else if (token.equals("!create")) {
				return new CommandCreate(st, line);
			} else if (token.equals("!bid")) {
				return new CommandBid(st, line);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}

		throw new IllegalArgumentException();
	}

	protected Command(Cmd id, String cmd) {
		this.id = id;
		this.command = cmd;
	}

	public Cmd getId() {
		return id;
	}

	public String getCommand() {
		return command;
	}

	@Override
	public String toString() {
		return id.toString();
	}
}

class CommandLogin extends Command {

	private final String user;
	private final int udpPort;

	protected CommandLogin(StringTokenizer st, String line) {
		super(Cmd.LOGIN, line);

		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		this.user = st.nextToken();
		this.udpPort = Integer.parseInt(st.nextToken());
	}
}

class CommandCreate extends Command {

	private final int duration;
	private final String description;

	protected CommandCreate(StringTokenizer st, String line) {
		super(Cmd.CREATE, line);

		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		this.duration = Integer.parseInt(st.nextToken());
		this.description = st.nextToken();
	}
}

class CommandBid extends Command {

	private final int auctionId;
	private final int amount;

	protected CommandBid(StringTokenizer st, String line) {
		super(Cmd.BID, line);

		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		this.auctionId = Integer.parseInt(st.nextToken());
		this.amount = Integer.parseInt(st.nextToken());
	}
}