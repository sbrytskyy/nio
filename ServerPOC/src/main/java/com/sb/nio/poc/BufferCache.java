package com.sb.nio.poc;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferCache {

	private static final Logger log = LoggerFactory.getLogger(BufferCache.class);

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	private static final int LARGE_BUFFER_SIZE = 8192;

	private Queue<ByteBuffer> buffers = new ConcurrentLinkedQueue<>();
	private Queue<ByteBuffer> largeBuffers = new ConcurrentLinkedQueue<>();

	private AtomicLong leased = new AtomicLong();
	private AtomicLong leasedLarge = new AtomicLong();
	
	private static BufferCache instance = new BufferCache();
	
	private BufferCache() {
	}

	public static BufferCache getInstance() {
		return instance;
	}

	public ByteBuffer leaseBuffer() {
		ByteBuffer buffer = buffers.poll();
		if (buffer == null) {
			buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
		}
		long l = leased.incrementAndGet();

		log.debug("Leased buffers: {}", l);

		buffer.clear();
		return buffer;
	}

	public void returnBuffer(ByteBuffer buffer) {
		leased.decrementAndGet();
		buffer.clear();
		buffers.add(buffer);
		
		log.debug("Cached buffers: {}", buffers.size());
	}

	public ByteBuffer leaseLargeBuffer() {
		ByteBuffer buffer = largeBuffers.poll();
		if (buffer == null) {
			buffer = ByteBuffer.allocate(LARGE_BUFFER_SIZE);
		}
		long l = leasedLarge.incrementAndGet();

		log.debug("Leased large buffers: {}", l);

		buffer.clear();
		return buffer;
	}

	public void returnLargeBuffer(ByteBuffer buffer) {
		leasedLarge.decrementAndGet();
		buffer.clear();
		largeBuffers.add(buffer);
		
		log.debug("Cached large buffers: {}", largeBuffers.size());
	}
}
