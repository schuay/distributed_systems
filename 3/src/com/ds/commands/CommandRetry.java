package com.ds.commands;

import java.io.Serializable;
import java.util.StringTokenizer;

public class CommandRetry extends Command {
    public CommandRetry() {
        super("!retry", Cmd.RETRY);
    }
}
