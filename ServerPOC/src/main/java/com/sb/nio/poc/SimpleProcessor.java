package com.sb.nio.poc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProcessor implements ProtocolProcessor {

	private static final Logger log = LoggerFactory.getLogger(SimpleProcessor.class);
	private Queue<Message> outboundMessageQueue;
	private Selector selector;

	private BlockingQueue<IncomingData> incoming = new LinkedBlockingQueue<>();

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
		this.outboundMessageQueue = outboundMessageQueue;
		this.selector = selector;
	}

	@Override
	public void run() {
		while (true) {
			try {
				IncomingData data = incoming.take();
				
				ByteBuffer readBuffer = data.getReadBuffer();

				String s = new String(readBuffer.array());
				log.debug("Incoming data: <<<\n{}>>>", s);

				String httpResponse = "HTTP/1.1 200 OK\r\n" + "Content-Length: 38\r\n" + "Content-Type: text/html\r\n"
						+ "\r\n" + "<html><body>Hello World!</body></html>";

				byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");

				SocketContainer sc = data.getSocketContainer();
				Message message = new Message(sc);
				message.setBody(httpResponseBytes);

				outboundMessageQueue.add(message);
				selector.wakeup();

			} catch (InterruptedException | UnsupportedEncodingException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

}
