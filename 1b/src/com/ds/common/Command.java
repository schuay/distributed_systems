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

	public static Command parse(String line) {
		StringTokenizer st = new StringTokenizer(line);
		if (!st.hasMoreTokens()) {
			throw new IllegalArgumentException();
		}

		try {
			String token = st.nextToken().toLowerCase();
			if (token.equals("!logout")) {
				return new Command(Cmd.LOGOUT);
			} else if (token.equals("!list")) {
				return new Command(Cmd.LIST);
			} else if (token.equals("!end")) {
				return new Command(Cmd.END);
			} else if (token.equals("!login")) {
				return CommandLogin.parse(st);
			} else if (token.equals("!create")) {
				return CommandCreate.parse(st);
			} else if (token.equals("!bid")) {
				return CommandBid.parse(st);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}

		throw new IllegalArgumentException();
	}

	protected Command(Cmd id) {
		this.id = id;
	}

	public Cmd getId() {
		return id;
	}

	@Override
	public String toString() {
		return id.toString();
	}
}

class CommandLogin extends Command {

	protected static Command parse(StringTokenizer st) {
		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		final String user = st.nextToken();
		final int udpPort = Integer.parseInt(st.nextToken());

		return new CommandLogin(user, udpPort);
	}

	protected CommandLogin(String user, int udpPort) {
		super(Cmd.LOGIN);
	}
}

class CommandCreate extends Command {

	protected static Command parse(StringTokenizer st) {
		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		final int duration = Integer.parseInt(st.nextToken());
		final String description = st.nextToken();

		return new CommandCreate(duration, description);
	}

	protected CommandCreate(int duration, String description) {
		super(Cmd.CREATE);
	}
}

class CommandBid extends Command {

	protected static Command parse(StringTokenizer st) {
		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		final int auctionId = Integer.parseInt(st.nextToken());
		final int amount = Integer.parseInt(st.nextToken());

		return new CommandBid(auctionId, amount);
	}

	protected CommandBid(int auctionId, int amount) {
		super(Cmd.BID);
	}
}