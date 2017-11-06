package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class Message {

	private ByteBuffer body;
	private long socketId;

	public Message(ByteBuffer buffer, long socketId) {
		body = buffer;
		this.socketId = socketId;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public long getSocketId() {
		return socketId;
	}
}
