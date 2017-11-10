package com.sb.nio.poc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final int DEFAULT_SERVER_PORT = 8080;

	public void startServer() {
		Server s = new Server(DEFAULT_SERVER_PORT, Protocol.HTTP);
		try {
			s.start();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.startServer();
	}
}
