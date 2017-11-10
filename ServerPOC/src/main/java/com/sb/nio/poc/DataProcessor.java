package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor implements IDataProcessor, DataProcessorCallback {

	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

	private BlockingQueue<Socket> incoming = new LinkedBlockingQueue<>();

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	private MessageListener listener;

	private Map<Socket, ByteBuffer> socketCachedData;

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

				// TODO Redesign using Factory pattern
				ProtocolProcessor t = new HttpSimpleProcessor(data, DataProcessor.this);
				executor.execute(t);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public synchronized void messageReady(Message message) {
		listener.messageReady(message);
	}
}
