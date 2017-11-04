package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class Message {

	private SocketContainer sc;
	private ByteBuffer body;

	public Message(SocketContainer sc, ByteBuffer body) {
		this.sc = sc;
		this.body = body;
	}

	public SocketContainer getSc() {
		return sc;
	}

	public ByteBuffer getBody() {
		return body;
	}
}
