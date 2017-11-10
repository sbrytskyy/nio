package com.sb.nio.poc;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProtocolProcessor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ProtocolProcessor.class);

	private static BufferCache cache = BufferCache.getInstance();

	private IncomingData data;
	private DataProcessorCallback callback;

	public ProtocolProcessor(IncomingData data, DataProcessorCallback callback) {
		this.data = data;
		this.callback = callback;
	}

	protected class Response {
		boolean ready;
		boolean keepAlive;
		byte[] body;
		int readBytes;
	}

	public void run() {
		ByteBuffer readBuffer = data.getReadBuffer();
		log.trace("Buffer size: {}", readBuffer.remaining());

		synchronized (readBuffer) {
			Response response = prepareResponse(readBuffer);
			if (response.ready) {
				// Shift not used data to the beginning
				int oldPosition = readBuffer.position();
				readBuffer.position(response.readBytes);
				readBuffer.compact();
				readBuffer.position(oldPosition - response.readBytes);
				log.trace("Read cache buffer after compacting: {}", readBuffer);

				ByteBuffer writeBuffer = cache.leaseLargeBuffer();
				writeBuffer.clear();
				writeBuffer.put(response.body);
				writeBuffer.flip();

				Message message = new Message(writeBuffer, data.getSocket(), response.keepAlive);
				callback.messageReady(message);
			}
		}
	}

	protected abstract Response prepareResponse(final ByteBuffer readBuffer);
}
