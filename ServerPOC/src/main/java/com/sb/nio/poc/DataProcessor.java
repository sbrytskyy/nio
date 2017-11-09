package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor implements IDataProcessor, DataProcessorCallback {

	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);
	
	private BlockingQueue<IncomingData> incoming = new LinkedBlockingQueue<>();
	
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

	private static BufferCache cache = BufferCache.getInstance();
	
	private Map<Socket, ByteBuffer> map = new HashMap<>();

	private MessageListener listener;

	@Override
	public void setMessageListener(MessageListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized void processData(IncomingData data) {
		
		ByteBuffer readBuffer = data.getReadBuffer();
		Socket socket = data.getSocket();

		String s = new String(readBuffer.array());
		log.debug("[DataProcessor] data to process : <<< ---\n{}\n--- >>>", s);

		ByteBuffer buffer;
		if (map.containsKey(socket)) {
			log.trace("Cached buffer for socket: {}", socket);
			buffer = map.get(socket);
		} else {
			buffer = cache.leaseLargeBuffer();
			log.trace("New buffer for socket: {}", socket);
		}
		// TODO Think about limit check, maybe resizable buffer
		buffer.put(readBuffer);
		cache.returnBuffer(readBuffer);
		map.put(socket, buffer);
		
		
		s = new String(buffer.array());
		log.debug("[DataProcessor] total data to process : <<< ---\n{}\n--- >>>", s);
		log.trace("Cached buffer size: {}", buffer.remaining());
		
		// TODO check if setter is better
		// TODO FIX Choose
		// 1. Put unique data to queue, but it require data copying.
		// 2. Check if data complete before put to queue, but it require additional method to check. NO
		// 3. Put socket to queue and retrieve data from map - MAYBE Top priority to check

		data = new IncomingData(buffer, socket);
		try {
			incoming.put(data);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				IncomingData data = incoming.take();

				// TODO Redesign
				ProtocolProcessor t = new HttpSimpleProcessor(data, DataProcessor.this);
				executor.execute(t);
			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void messageReady(Message message) {
		// think what to do with leftover
		
		cache.returnLargeBuffer(map.get(message.getSocket()));;
		map.remove(message.getSocket());

		listener.messageReady(message);
	}
}
