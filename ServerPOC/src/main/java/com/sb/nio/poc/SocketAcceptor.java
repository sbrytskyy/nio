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
	private int port;
	
	private ServerSocketChannel serverSocketChannel;
	private Queue<SocketContainer> queue;

	public SocketAcceptor(int port, Queue<SocketContainer> queue) {
		this.port = port;
		this.queue = queue;
	}

	@Override
	public void run() {
		try  {
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(port));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return;
		}
		
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
