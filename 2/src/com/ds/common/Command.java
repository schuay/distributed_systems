package com.ds.common;

import java.io.Serializable;
import java.util.StringTokenizer;

import com.ds.client.ParsedArgs;

public class Command implements Serializable {

	private static final long serialVersionUID = 4698051331006702570L;

	public enum Cmd {
		LOGIN,
		LOGOUT,
		LIST,
		CREATE,
		BID,
		END
	}

	private final Cmd id;

	public static Command parse(ParsedArgs args, String line) {
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
				return new CommandLogin(st, args);
			} else if (token.equals("!create")) {
				return new CommandCreate(st);
			} else if (token.equals("!bid")) {
				return new CommandBid(st);
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