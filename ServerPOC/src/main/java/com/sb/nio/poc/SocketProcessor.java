package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketProcessor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SocketProcessor.class);

	private Queue<SocketContainer> inboundPortsQueue;
	
	private Queue<Message> outboundMessageQueue;

	private Selector selector;

	private ProtocolProcessor protocolProcessor;

	public SocketProcessor(Queue<SocketContainer> inboundPortsQueue, ProtocolProcessor protocolProcessor, Selector selector) throws IOException {
		this.inboundPortsQueue = inboundPortsQueue;
		this.protocolProcessor = protocolProcessor;
		this.selector = selector;
		
		outboundMessageQueue = new ConcurrentLinkedQueue<>();
		protocolProcessor.init(outboundMessageQueue, selector);
	}

	@Override
	public void run() {
		while (true) {
			try {
				int select = selector.select();
								
				if (select > 0) {
					Set<SelectionKey> keys = selector.selectedKeys();

					Iterator<SelectionKey> it = keys.iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						
						if (!key.isValid()) continue;

						if (key.isReadable()) {
							readFromSocket(key);
						}
						
						if (key.isWritable()) {
							writeToSocket(key);
						}
					}
				}
				
				processSockets();
				processMessages();

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void processMessages() throws IOException {
		while (!outboundMessageQueue.isEmpty()) {
			Message message = outboundMessageQueue.poll();
			SelectionKey key = message.getSc().getChannel().register(selector, SelectionKey.OP_WRITE);
			key.attach(message);
		}
	}

	private void processSockets() throws IOException {
		while (!inboundPortsQueue.isEmpty()) {
			SocketContainer sc = inboundPortsQueue.poll();
			sc.getChannel().configureBlocking(false);
			SelectionKey key = sc.getChannel().register(selector, SelectionKey.OP_READ);
			key.attach(sc);
		}
	}

	private void readFromSocket(SelectionKey key) throws IOException {
		SocketContainer sc = (SocketContainer) key.attachment();

		readData(sc);

		if (sc.isEndOfStreamReached()) {
			log.debug("Socket has been closed: {}", sc.getChannel());

			key.attach(null);
			key.cancel();
			key.channel().close();
		}
	}

	private void writeToSocket(SelectionKey key) throws IOException {
		Message message = (Message) key.attachment();

		int written = message.getSc().write(message.getBody());
		log.debug("Outbound message to {}, written {} bytes.", message.getSc().getChannel(), written);
		
		message.cleanup();

		key.attach(null);
		key.cancel();
		key.channel().close();
	}

	private void readData(SocketContainer sc) throws IOException {

		ByteBuffer readBuffer = ByteBuffer.allocate(2048);

		int bytesRead = sc.read(readBuffer);
		readBuffer.flip();

		if (bytesRead > 0) {
			IncomingData data = new IncomingData(readBuffer, sc);
			protocolProcessor.processData(data);
		}

		if (readBuffer.remaining() == 0) {
			readBuffer.clear();
			return;
		}
		readBuffer.clear();
	}
}
