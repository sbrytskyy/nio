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

	public void run() {
		ByteBuffer readBuffer = data.getReadBuffer();
		boolean keepAlive = isKeepAlive(readBuffer);
		
		ByteBuffer writeBuffer = cache.leaseBuffer();
		boolean ready = prepareResponse(readBuffer, writeBuffer);
		if (ready) {
			Message message = new Message(writeBuffer, data.getSocketId(), keepAlive);
			callback.messageReady(message);
		}
	}

	protected abstract boolean prepareResponse(final ByteBuffer readBuffer, ByteBuffer writeBuffer);

	protected abstract boolean isKeepAlive(ByteBuffer readBuffer);
}

