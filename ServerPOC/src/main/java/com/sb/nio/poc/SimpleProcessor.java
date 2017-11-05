package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProcessor implements ProtocolProcessor {

	private static final Logger log = LoggerFactory.getLogger(SimpleProcessor.class);
	private static Queue<Message> outboundMessageQueue;
	private static Selector selector;

	private BlockingQueue<IncomingData> incoming = new LinkedBlockingQueue<>();

	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

	@Override
	public void processData(IncomingData data) throws IOException {
		try {
			incoming.put(data);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void init(Queue<Message> outboundMessageQueue, Selector selector) {
		SimpleProcessor.outboundMessageQueue = outboundMessageQueue;
		SimpleProcessor.selector = selector;
	}

	@Override
	public void run() {
		while (true) {
			try {
				IncomingData data = incoming.take();

				Task t = new Task(data);
				executor.execute(t);

			} catch (InterruptedException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	static class Task implements Runnable {

		private IncomingData data;

		public Task(IncomingData data) {
			this.data = data;
		}

		public void run() {
			ByteBuffer buffer = data.getReadBuffer();

			String s = new String(buffer.array());
			log.debug("Incoming data: <<<\n{}>>>", s);

			String httpResponse = "HTTP/1.1 200 OK\r\n" + "Content-Length: 38\r\n" + "Content-Type: text/html\r\n"
					+ "\r\n" + "<html><body>Hello World!</body></html>";

			byte[] httpResponseBytes = httpResponse.getBytes();

			buffer.clear();
			buffer.put(httpResponseBytes);
			buffer.flip();

			SocketContainer sc = data.getSocketContainer();
			Message message = new Message(sc, buffer);

			outboundMessageQueue.add(message);
			selector.wakeup();
		}
	}

}
