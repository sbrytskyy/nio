package com.sb.nio.poc;

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
	
	private Map<Long, ByteBuffer> map = new HashMap<>();

	private MessageListener listener;

	@Override
	public void setMessageListener(MessageListener listener) {
		this.listener = listener;
	}

	@Override
	public synchronized void processData(IncomingData data) {
		
		String s = new String(data.getReadBuffer().array());
		data.getReadBuffer().flip();
		log.debug("[DataProcessor] data to process : <<< ---\n{}\n--- >>>", s);

		
		ByteBuffer buffer;
		if (map.containsKey(data.getSocketId())) {
			log.debug("Cached buffer for socket: {}", data.getSocketId());
			buffer = map.get(data.getSocketId());
		} else {
			buffer = cache.leaseLargeBuffer();
			log.debug("New buffer for socket: {}", data.getSocketId());
		}
		// TODO Think about limit check, maybe resizable buffer
		buffer.put(data.getReadBuffer());
		cache.returnBuffer(data.getReadBuffer());
		buffer.flip();
		map.put(data.getSocketId(), buffer);
		
		
		s = new String(buffer.array());
		buffer.flip();
		log.debug("[DataProcessor] total data to process : <<< ---\n{}\n--- >>>", s);
		log.debug("Cached buffer size: {}", buffer.remaining());
		
		// TODO check if setter is better
		data = new IncomingData(buffer, data.getSocketId());
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
		
		cache.returnLargeBuffer(map.get(message.getSocketId()));;
		map.remove(message.getSocketId());

		listener.messageReady(message);
	}
}
