package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class Message {

	private SocketContainer sc;
	private ByteBuffer body;

	public Message(SocketContainer sc) {
		this.sc = sc;
		
		// TODO lease ByteBuffer from cache
		body = ByteBuffer.allocate(2048);
	}

	public SocketContainer getSc() {
		return sc;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public void cleanup() {
		// return ByteBuffer
	}

	public void setBody(byte[] ba) {
		body.clear();
		body.put(ba);
		body.flip();
	}
}
