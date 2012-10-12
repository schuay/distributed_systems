package com.ds.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import com.ds.common.Response;

public class ResponseThread implements Runnable {

	private final Socket socket;

	public ResponseThread(Socket socket) throws IOException {
		this.socket = socket;
	}

	@Override
	public void run() {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(socket.getInputStream());

			Response response;
			while ((response = (Response)in.readObject()) != null) {
				System.out.println(response);
			}
		} catch (Exception e) {
			e.getMessage();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
