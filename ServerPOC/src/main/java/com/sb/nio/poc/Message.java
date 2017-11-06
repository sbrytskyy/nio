package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class Message {

	private ByteBuffer body;
	private long socketId;
	private boolean keepAlive;

	public Message(ByteBuffer buffer, long socketId, boolean keepAlive) {
		this.body = buffer;
		this.socketId = socketId;
		this.keepAlive = keepAlive;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public long getSocketId() {
		return socketId;
	}
}
