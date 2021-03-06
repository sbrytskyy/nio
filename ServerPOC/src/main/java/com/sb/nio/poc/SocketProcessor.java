package com.sb.nio.poc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketProcessor implements Runnable, MessageListener {

	private static final Logger log = LoggerFactory.getLogger(SocketProcessor.class);

	private Map<Socket, SocketChannel> socketsMap = new HashMap<>();

	private Map<Socket, ByteBuffer> socketCachedData = Collections.synchronizedMap(new HashMap<>());

	private Queue<Message> outboundMessageQueue;

	private ServerSocketChannel serverSocketChannel;
	private Selector selector;

	private IDataProcessor protocolProcessor;

	private BufferCache cache = BufferCache.getInstance();

	public SocketProcessor(IDataProcessor protocolProcessor, int port) throws IOException {

		this.protocolProcessor = protocolProcessor;

		this.selector = Selector.open();
		this.outboundMessageQueue = new ConcurrentLinkedQueue<>();

		protocolProcessor.setMessageListener(this);
		protocolProcessor.setSocketCachedData(socketCachedData);

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

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				break;
			}
		}
	}

	private void acceptSocket(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		if (socketChannel != null) {
			log.debug("Socket accepted: " + socketChannel);
			socketChannel.configureBlocking(false);

			socketChannel.register(selector, SelectionKey.OP_READ);
		}
	}

	private void processMessages() throws IOException {
		while (!outboundMessageQueue.isEmpty()) {
			Message message = outboundMessageQueue.poll();
			SocketChannel channel = socketsMap.get(message.getSocket());
			channel.register(selector, SelectionKey.OP_WRITE, message);
		}
	}

	private void readFromSocket(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		boolean result = true;
		
		ByteBuffer readBuffer = cache.leaseBuffer();
		try {
			int bytesRead = channel.read(readBuffer);

			if (bytesRead == -1) {
				log.debug("Socket has been closed by client: {}", channel);
				result = false;
			} else {
				Socket socket = channel.socket();
				socketsMap.put(socket, channel);

				readBuffer.flip();

				ByteBuffer buffer;
				if (socketCachedData.containsKey(socket)) {
					buffer = socketCachedData.get(socket);
				} else {
					buffer = cache.leaseLargeReadBuffer();
				}
				synchronized (buffer) {
					// TODO Think about limit check, maybe resizable buffer
					buffer.put(readBuffer);
					socketCachedData.put(socket, buffer);
				}
				cache.returnBuffer(readBuffer);

				protocolProcessor.processData(socket);
			}
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
			result = false;
		}

		if (!result) {
			cleanupCache(channel);

			key.channel().close();
			key.cancel();

			cache.returnBuffer(readBuffer);
		}
	}

	private void writeToSocket(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		Message message = (Message) key.attachment();

		ByteBuffer byteBuffer = message.getBody();

		int totalWritten = 0;

		while (byteBuffer.hasRemaining()) {
			int written = channel.write(byteBuffer);
			totalWritten += written;
		}

		log.debug("Outbound message to {}, written {} bytes.", channel, totalWritten);

		cache.returnLargeWriteBuffer(byteBuffer);
		key.attach(null);

		if (message.isKeepAlive()) {
			key.interestOps(SelectionKey.OP_READ);
		} else {
			cleanupCache(channel);

			key.channel().close();
			key.cancel();
		}
	}

	private void cleanupCache(SocketChannel channel) {
		cache.returnLargeReadBuffer(socketCachedData.get(channel.socket()));
		socketCachedData.remove(channel.socket());
	}

	@Override
	public void messageReady(Message message) {
		outboundMessageQueue.add(message);
		selector.wakeup();
	}
}
