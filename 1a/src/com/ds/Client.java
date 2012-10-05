package com.ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	private static final String HOST = "stockholm.vitalab.tuwien.ac.at";
	private static final int PORT = 9000;

	public static void main(String[] args) throws IOException {
		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		
		try {
			socket = new Socket(HOST, PORT);
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.printf("Don't know about host: %s.%n", HOST);
            System.exit(1);
        } catch (IOException e) {
            System.err.printf("Couldn't get I/O for the connection to: %s: %s%n",
            		HOST, e.getMessage());
            System.exit(1);
        }
		
		out.printf("!login %s %s");
		
		String response;
		while ((response = in.readLine()) != null) {
			System.out.println(response);
		}
		
		out.close();
		in.close();
		socket.close();
	}

}
