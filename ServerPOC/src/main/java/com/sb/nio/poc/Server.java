package com.sb.nio.poc;

import java.io.IOException;
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

	public void start() throws IOException {
		log.info("Server configured with port: {}", port);

		SocketAcceptor ssa = new SocketAcceptor(port, inboundPortsQueue);
		Thread accepterThread = new Thread(ssa);
		accepterThread.start();
		
		SocketProcessor sp = new SocketProcessor(inboundPortsQueue);
		Thread processorThread = new Thread(sp);
		processorThread.start();
	}
}
