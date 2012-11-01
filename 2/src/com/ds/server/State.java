package com.ds.server;

import com.ds.common.Command;

public interface State {
	void processCommand(Command command);
}
