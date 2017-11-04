package com.sb.nio.poc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketProcessor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SocketProcessor.class);

	private Queue<Message> outboundMessageQueue;

	private ServerSocketChannel serverSocketChannel;
	private Selector selector;

	private ProtocolProcessor protocolProcessor;

	private BufferCache cache;

	public SocketProcessor(ProtocolProcessor protocolProcessor, int port) throws IOException {

		this.protocolProcessor = protocolProcessor;
		this.cache = new BufferCache();

		this.selector = Selector.open();
		outboundMessageQueue = new ConcurrentLinkedQueue<>();
		protocolProcessor.init(outboundMessageQueue, selector);

		serverSocketInit(port);
	}

	private void serverSocketInit(int port) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.bind(new InetSocketAddress(port));
		serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
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
						it.remove();

						if (!key.isValid())
							continue;

						if (key.isAcceptable()) {
							acceptSocket(key);
						} else if (key.isReadable()) {
							readFromSocket(key);
						} else if (key.isWritable()) {
							writeToSocket(key);
						}
					}
				}

				processMessages();

			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private void acceptSocket(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel != null) {
			log.debug("Socket accepted: " + socketChannel);
			socketChannel.configureBlocking(false);

			SocketContainer sc = new SocketContainer(socketChannel);

			SelectionKey keyRead = socketChannel.register(selector, SelectionKey.OP_READ);
			keyRead.attach(sc);
		}
	}

	private void processMessages() throws IOException {
		while (!outboundMessageQueue.isEmpty()) {
			Message message = outboundMessageQueue.poll();
			SelectionKey key = message.getSc().getChannel().register(selector, SelectionKey.OP_WRITE);
			key.attach(message);
		}
	}

	private void readFromSocket(SelectionKey key) throws IOException {
		SocketContainer sc = (SocketContainer) key.attachment();

		readData(sc);

		if (sc.isEndOfStreamReached()) {
			log.debug("Socket has been closed: {}", sc.getChannel());

			key.attach(null);
			key.channel().close();
			key.cancel();
		}
	}

	private void writeToSocket(SelectionKey key) throws IOException {
		Message message = (Message) key.attachment();

		ByteBuffer buffer = message.getBody();
		int written = message.getSc().write(buffer);
		log.debug("Outbound message to {}, written {} bytes.", message.getSc().getChannel(), written);

		cache.returnBuffer(buffer);

		key.attach(null);
		key.channel().close();
		key.cancel();
	}

	private void readData(SocketContainer sc) throws IOException {

		ByteBuffer readBuffer = cache.leaseBuffer();

		int bytesRead = sc.read(readBuffer);
		if (bytesRead > 0) {
			IncomingData data = new IncomingData(readBuffer, sc);
			protocolProcessor.processData(data);
		} else {
			cache.returnBuffer(readBuffer);
		}
	}
}
