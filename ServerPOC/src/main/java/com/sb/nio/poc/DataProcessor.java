package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor implements IDataProcessor {

	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

	private BlockingQueue<Socket> incoming = new LinkedBlockingQueue<>();

	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
			.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 2, 1));

	private MessageListener listener;

	private Map<Socket, ByteBuffer> socketCachedData;

	private Protocol protocol;

	public DataProcessor(Protocol protocol) {
		this.protocol = protocol;
	}

	@Override
	public void setSocketCachedData(Map<Socket, ByteBuffer> socketCachedData) {
		this.socketCachedData = socketCachedData;
	}

	@Override
	public void setMessageListener(MessageListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized void processData(Socket socket) {
		try {
			incoming.put(socket);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Socket socket = incoming.take();

				ByteBuffer buffer = socketCachedData.get(socket);
				IncomingData data = new IncomingData(buffer, socket);

				ProtocolProcessor t = ProtocolProcessorFactory.getProtocolProcessor(protocol);
				t.setData(data, listener);
				executor.execute(t);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
