package com.sb.nio.poc;

public class Message {

	private SocketContainer sc;
	private byte[] httpResponseBytes;

	public Message(SocketContainer sc) {
		this.sc = sc;
	}

	public SocketContainer getSc() {
		return sc;
	}

	public void setBody(byte[] httpResponseBytes) {
		this.httpResponseBytes = httpResponseBytes;
	}

	public byte[] getBody() {
		return httpResponseBytes;
	}
}
