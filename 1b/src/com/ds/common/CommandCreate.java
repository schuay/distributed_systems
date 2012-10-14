package com.ds.common;

import java.util.StringTokenizer;

public class CommandCreate extends Command {

	private static final long serialVersionUID = -1971848687816624645L;

	private final int duration;
	public int getDuration() {
		return duration;
	}

	private final String description;
	public String getDescription() {
		return description;
	}

	protected CommandCreate(StringTokenizer st) {
		super(Cmd.CREATE);

		if (st.countTokens() < 2) {
			throw new IllegalArgumentException();
		}

		this.duration = Integer.parseInt(st.nextToken());
		this.description = st.nextToken();
	}
}