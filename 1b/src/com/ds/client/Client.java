package com.ds.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.ds.common.Command;
import com.ds.common.Command.Cmd;

public class Client {

	private static final String INDENT = "> ";

	public static void main(String[] args) throws IOException {

		ParsedArgs parsedArgs;
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

		Socket socket = null;
		ObjectOutputStream out = null;
		try {
			socket = new Socket(parsedArgs.getHost(), parsedArgs.getTcpPort());

			Thread responseThread = new Thread(new ResponseThread(socket));
			responseThread.start();

			out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Connection successful.");

			inputLoop(parsedArgs, out);

			responseThread.join();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();
		}
	}

	private static void inputLoop(ParsedArgs args, ObjectOutputStream out) throws IOException {
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		/* Initial indentation. */
		System.out.print(INDENT);

		String userInput;
		Command command;
		while ((userInput = stdin.readLine()) != null) {
			/* Parse and send the command. */
			try {
				command = Command.parse(args, userInput);
				out.writeObject(command);

				if (command.getId() == Cmd.END) {
					break;
				}

				System.out.print(INDENT);
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid command ignored");
			}
		}

		stdin.close();
	}
}
