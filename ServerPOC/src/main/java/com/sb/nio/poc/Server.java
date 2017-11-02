package com.sb.nio.poc;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private static final int MAX_INBOUND_CONNECTIONS = 1024;

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private Queue<SocketContainer> inboundPortsQueue;

	public Server(int port) {
		this.port = port;
		inboundPortsQueue = new ArrayBlockingQueue<>(MAX_INBOUND_CONNECTIONS);
	}

	public void start() {
		log.info("Server configured with port: {}", port);

		ServerSocketAcceptor ssa = new ServerSocketAcceptor(port, inboundPortsQueue);
		Thread accepterThread = new Thread(ssa);
		accepterThread.start();
	}

}
