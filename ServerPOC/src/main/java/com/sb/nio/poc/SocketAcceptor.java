package com.sb.nio.poc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketAcceptor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SocketAcceptor.class);
	
	private ServerSocketChannel serverSocketChannel;
	private Queue<SocketContainer> queue;

	public SocketAcceptor(int port, Queue<SocketContainer> queue) throws IOException {
		this.queue = queue;
		
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
	}

	@Override
	public void run() {
		while (true) {
			try {
				SocketChannel channel = serverSocketChannel.accept();
				log.debug("Socket accepted: " + channel);
				
				SocketContainer sc = new SocketContainer(channel);
				queue.add(sc);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
