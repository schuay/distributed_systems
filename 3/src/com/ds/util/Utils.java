package com.ds.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Utils {

    private static final String EXIT_CMD = "!exit";

    private Utils() { }

    /**
     * Waits for the user to enter the exit command.
     */
    public static void waitForExit() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(isr);

        String line = null;
        do {
            try {
                line = in.readLine();
            } catch (IOException e) {}
        } while (!EXIT_CMD.equals(line));

        try { in.close(); } catch (IOException e) {}
        try { isr.close(); } catch (IOException e) {}
    }

    public static void printRunningMsg(String componentName) {
        System.out.println(String.format("%s started, enter '%s' to initiate shutdown.",
                    componentName, EXIT_CMD));
    }
}
