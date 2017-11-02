package com.sb.nio.poc;

public class Main {

	private static final int DEFAULT_SERVER_PORT = 8080;

	public void startServer() {
		Server s = new Server(DEFAULT_SERVER_PORT);
		s.start();
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.startServer();
	}
}
