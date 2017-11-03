package com.sb.nio.poc;

import java.nio.ByteBuffer;

public class IncomingData {

	private SocketContainer sc;
	private ByteBuffer readBuffer;

	public IncomingData(ByteBuffer readBuffer, SocketContainer sc) {
		this.readBuffer = readBuffer;
		this.sc = sc;
	}

	public SocketContainer getSocketContainer() {
		return sc;
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

}
