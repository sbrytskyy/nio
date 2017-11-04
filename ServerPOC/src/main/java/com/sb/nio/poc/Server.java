package com.sb.nio.poc;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private static final int MAX_INBOUND_CONNECTIONS = 1024;

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private Queue<SocketContainer> inboundPortsQueue;
	private ProtocolProcessor protocolProcessor;
	
	private BufferCache cache;
	
	public Server(int port) {
		this.port = port;
		inboundPortsQueue = new ArrayBlockingQueue<>(MAX_INBOUND_CONNECTIONS);
		
		cache = new BufferCache(MAX_INBOUND_CONNECTIONS * 2);
	}

	public void start() throws IOException {
		log.info("Server configured with port: {}", port);

		Selector selector = Selector.open();
		
		SocketAcceptor ssa = new SocketAcceptor(port, inboundPortsQueue, selector);
		Thread accepterThread = new Thread(ssa);
		accepterThread.start();
		
		protocolProcessor = new SimpleProcessor();
		Thread pp = new Thread(protocolProcessor);
		pp.start();
		
		SocketProcessor sp = new SocketProcessor(inboundPortsQueue, protocolProcessor, selector, cache);
		Thread processorThread = new Thread(sp);
		processorThread.start();
	}
}
