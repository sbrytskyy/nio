package com.sb.nio.poc;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Redesign cache for some kind of smart pointers - self containing info about what kind of cache, when to return to cache
public class BufferCache {

	private static final Logger log = LoggerFactory.getLogger(BufferCache.class);

	private static final int DEFAULT_BUFFER_SIZE = 2048;
	private static final int LARGE_BUFFER_SIZE = 8192 * 2;

	private Queue<ByteBuffer> buffers = new ConcurrentLinkedQueue<>();
	private Queue<ByteBuffer> largeReadBuffers = new ConcurrentLinkedQueue<>();
	private Queue<ByteBuffer> largeWriteBuffers = new ConcurrentLinkedQueue<>();

	private AtomicLong leased = new AtomicLong();
	private AtomicLong leasedLargeRead = new AtomicLong();
	private AtomicLong leasedLargeWrite = new AtomicLong();

	private static BufferCache instance = new BufferCache();

	private BufferCache() {
	}

	public static BufferCache getInstance() {
		return instance;
	}

	public ByteBuffer leaseBuffer() {
		ByteBuffer buffer = buffers.poll();
		long l = leased.incrementAndGet();

		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);
		}

		log.debug("Leased buffers: {}:{}", l, buffers.size());

		buffer.clear();
		return buffer;
	}

	public void returnBuffer(ByteBuffer buffer) {
		long l = leased.decrementAndGet();
		buffer.clear();
		buffers.add(buffer);

		log.debug("Cached buffers: {}:{}", l, buffers.size());
	}

	public ByteBuffer leaseLargeReadBuffer() {
		ByteBuffer buffer = largeReadBuffers.poll();
		long l = leasedLargeRead.incrementAndGet();

		if (buffer == null) {
			buffer = ByteBuffer.allocate(LARGE_BUFFER_SIZE);
		}

		log.debug("Leased large read buffers: {}:{}", l, largeReadBuffers.size());

		buffer.clear();
		return buffer;
	}

	public ByteBuffer leaseLargeWriteBuffer() {
		ByteBuffer buffer = largeWriteBuffers.poll();
		long l = leasedLargeWrite.incrementAndGet();

		if (buffer == null) {
			buffer = ByteBuffer.allocateDirect(LARGE_BUFFER_SIZE);
		}

		log.debug("Leased large write buffers: {}:{}", l, largeWriteBuffers.size());

		buffer.clear();
		return buffer;
	}

	public void returnLargeReadBuffer(ByteBuffer buffer) {
		if (buffer == null)
			return;

		long l = leasedLargeRead.decrementAndGet();
		buffer.clear();
		largeReadBuffers.add(buffer);
		log.debug("Cached large read buffers: {}:{}", l, largeReadBuffers.size());
	}

	public void returnLargeWriteBuffer(ByteBuffer buffer) {
		if (buffer == null)
			return;

		long l = leasedLargeWrite.decrementAndGet();
		buffer.clear();
		largeWriteBuffers.add(buffer);

		log.debug("Cached large write buffers: {}:{}", l, largeWriteBuffers.size());
	}
}
