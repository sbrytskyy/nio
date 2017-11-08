package com.sb.nio.poc;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessor implements IDataProcessor {

	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);
	
	private BlockingQueue<IncomingData> incoming = new LinkedBlockingQueue<>();
	
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

	private MessageListener listener;

	@Override
	public void setMessageListener(MessageListener listener) {
		this.listener = listener;
	}

	@Override
	public void processData(IncomingData data) {
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

				DataProcessingTask t = new DataProcessingTask(data, listener);
				executor.execute(t);

			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
