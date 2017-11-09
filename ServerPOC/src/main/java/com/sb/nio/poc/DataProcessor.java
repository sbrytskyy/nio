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
	public void processData(IncomingData data) {
		
		ByteBuffer buffer;
		if (map.containsKey(data.getSocketId())) {
			buffer = map.get(data.getSocketId());
		} else {
			// TODO Redesign with cache and resizable buffer
			buffer = ByteBuffer.allocate(8192);
		}
		// TODO Think about limit check
		buffer.put(data.getReadBuffer().array());
		cache.returnBuffer(data.getReadBuffer());

		buffer.flip();
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
		
		map.remove(message.getSocketId());

		listener.messageReady(message);
	}
}
