package com.sb.nio.poc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private int port;
	private IDataProcessor protocolProcessor;
	
	public Server(int port) {
		this.port = port;
	}

	public void start() throws IOException {
		log.info("Server configured with port: {}", port);

		protocolProcessor = new DataProcessor();
		Thread pp = new Thread(protocolProcessor);
		pp.setName("Protocol Processor");
		pp.start();
		
		SocketProcessor sp = new SocketProcessor(protocolProcessor, port);
		Thread processorThread = new Thread(sp);
		processorThread.setName("Socket Processor");
		processorThread.start();
	}
}
