package com.sb.test.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	private static final int DEFAULT_SERVER_PORT = 8080;
	
	void startServer() {
		Server s = new Server(DEFAULT_SERVER_PORT);
		try {
			s.start();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.startServer();
	}
}
