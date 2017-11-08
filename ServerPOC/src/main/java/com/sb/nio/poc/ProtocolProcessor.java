package com.sb.nio.poc;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProtocolProcessor implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(ProtocolProcessor.class);

	private static BufferCache cache = BufferCache.getInstance();

	private IncomingData data;
	private MessageListener listener;

	public ProtocolProcessor(IncomingData data, MessageListener listener) {
		this.data = data;
		this.listener = listener;
	}

	public void run() {
		ByteBuffer readBuffer = data.getReadBuffer();

		boolean keepAlive = isKeepAlive(readBuffer);
		
		// add buffer to cache using socket Id
		// check cached message if enough to proceed 
		
		// - return readbuffer to cache
		cache.returnBuffer(readBuffer);

		// if yes - proceed with response
		// if no - wait for other chunk
		// think what to do with leftover

		ByteBuffer writeBuffer = cache.leaseBuffer();
		boolean ready = prepareResponse(readBuffer, writeBuffer);
		if (ready) {
			Message message = new Message(writeBuffer, data.getSocketId(), keepAlive);
			listener.messageReady(message);
		}
	}

	protected abstract boolean prepareResponse(final ByteBuffer readBuffer, ByteBuffer writeBuffer);

	protected abstract boolean isKeepAlive(ByteBuffer readBuffer);
}

