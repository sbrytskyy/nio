package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProcessor implements ProtocolProcessor {

	private static final Logger log = LoggerFactory.getLogger(SimpleProcessor.class);
	private Queue<Message> outboundMessageQueue;

	@Override
	public void processData(ByteBuffer readBuffer, SocketContainer sc) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (readBuffer.hasRemaining()) {
			sb.append((char) readBuffer.get()); // read 1 byte at a time
		}
		log.info("Incoming data: <<<\n{}>>>", sb.toString());
		
		String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 38\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body>Hello World!</body></html>";

        byte[] httpResponseBytes = httpResponse.getBytes("UTF-8");
        
		Message message = new Message(sc);
		message.setBody(httpResponseBytes);
		
		outboundMessageQueue.add(message);
	}

	@Override
	public void setMessageQueue(Queue<Message> outboundMessageQueue) {
		this.outboundMessageQueue = outboundMessageQueue;
	}

}
