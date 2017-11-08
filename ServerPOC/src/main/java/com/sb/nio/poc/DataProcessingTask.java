package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Redesign to abstract
public class DataProcessingTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(DataProcessingTask.class);

	private static BufferCache cache = BufferCache.getInstance();

	private IncomingData data;
	private MessageListener listener;

	public DataProcessingTask(IncomingData data, MessageListener listener) {
		this.data = data;
		this.listener = listener;
	}

	public void run() {
		// - Read buffer
		ByteBuffer readBuffer = data.getReadBuffer();
		
		// add buffer to cache using socket Id
		// check cached message if enough to proceed 
		
		
		// if yes - proceed with response
		// if no - wait for other chunk
		// think what to do with leftover

		// - proceed buffer
		String s = new String(readBuffer.array());
		log.debug("Incoming data: <<<\n{}>>>", s);

		boolean keepAlive = false;
		try {
			HttpRequest request = HttpHelper.create(readBuffer);
			keepAlive = HttpHelper.isKeepAlive(request);
		} catch (IOException | HttpException e) {
			log.error(e.getMessage(), e);
		}
		
		// - return readbuffer to cache
		
		cache.returnBuffer(readBuffer);
		
		// - Preparing response
		ByteBuffer writeBuffer = cache.leaseBuffer();
		String httpResponse = "HTTP/1.1 200 OK\r\n" + "Content-Length: 38\r\n" + "Content-Type: text/html\r\n"
				+ "\r\n" + "<html><body>Hello World!</body></html>";

		byte[] httpResponseBytes = httpResponse.getBytes();

		writeBuffer.clear();
		writeBuffer.put(httpResponseBytes);
		writeBuffer.flip();
		
		Message message = new Message(writeBuffer, data.getSocketId(), keepAlive);

		listener.messageReady(message);
	}
}

