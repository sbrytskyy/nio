package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;

public class Message {

	private ByteBuffer body;
	private Socket socket;
	private boolean keepAlive;

	public Message(ByteBuffer buffer, Socket socket, boolean keepAlive) {
		this.body = buffer;
		this.socket = socket;
		this.keepAlive = keepAlive;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public Socket getSocket() {
		return socket;
	}
}
