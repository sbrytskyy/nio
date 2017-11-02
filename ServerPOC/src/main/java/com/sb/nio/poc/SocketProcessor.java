package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketProcessor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SocketProcessor.class);

	private Queue<SocketContainer> inboundPortsQueue;

	private Selector readSelector;

	public SocketProcessor(Queue<SocketContainer> inboundPortsQueue) throws IOException {
		this.inboundPortsQueue = inboundPortsQueue;

		readSelector = Selector.open();
	}

	@Override
	public void run() {
		while (true) {
			try {
				processCycle();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	private void processCycle() throws IOException {
		processSockets();
		readFromSockets();
		writeToSockets();
	}

	private void processSockets() throws IOException {
		while (!inboundPortsQueue.isEmpty()) {
			SocketContainer sc = inboundPortsQueue.poll();
			sc.getChannel().configureBlocking(false);
			SelectionKey key = sc.getChannel().register(readSelector, SelectionKey.OP_READ);
			key.attach(sc);
		}
	}

	private void readFromSockets() throws IOException {
		int ready = readSelector.selectNow();
		if (ready > 0) {
			Set<SelectionKey> keys = readSelector.selectedKeys();

			Iterator<SelectionKey> it = keys.iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();

				readFromSocket(key);

				it.remove();
			}
			keys.clear();
		}
	}

	private void readFromSocket(SelectionKey key) throws IOException {
		SocketContainer sc = (SocketContainer) key.attachment();

		ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

		read(sc, byteBuffer);

		if (sc.isEndOfStreamReached()) {
			log.debug("Socket has been closed: {}", sc.getChannel());
			
			key.attach(null);
			key.cancel();
			key.channel().close();
		}
	}

	private void read(SocketContainer sc, ByteBuffer byteBuffer) throws IOException {
		int bytesRead = sc.read(byteBuffer);
		byteBuffer.flip();

		if (bytesRead > 0) {
			StringBuilder sb = new StringBuilder();
			while (byteBuffer.hasRemaining()) {
				sb.append((char) byteBuffer.get()); // read 1 byte at a time
			}
			log.info("Incoming data: <<<\n{}>>>", sb.toString());
		}

		if (byteBuffer.remaining() == 0) {
			byteBuffer.clear();
			return;
		}
		byteBuffer.clear();
	}

	private void writeToSockets() throws IOException {
	}
}
