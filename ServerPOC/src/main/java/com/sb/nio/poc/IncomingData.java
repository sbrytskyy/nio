package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class IncomingData {

	private ByteBuffer readBuffer;
	private long socketId;

	public IncomingData(ByteBuffer readBuffer, long socketId) {
		this.readBuffer = readBuffer;
		this.socketId = socketId;
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public long getSocketId() {
		return socketId;
	}

}
