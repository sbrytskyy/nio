package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;

public class IncomingData {

	private ByteBuffer readBuffer;
	private Socket socket;

	public IncomingData(ByteBuffer readBuffer, Socket socket) {
		this.readBuffer = readBuffer;
		this.socket = socket;
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public Socket getSocket() {
		return socket;
	}

}
