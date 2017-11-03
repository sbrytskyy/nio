package com.sb.nio.poc;

public class IncomingData {

	private SocketContainer sc;
	private byte[] bytesArray;

	public IncomingData(byte[] bytesArray, SocketContainer sc) {
		this.bytesArray = bytesArray;
		this.sc = sc;
	}

	public SocketContainer getSocketContainer() {
		return sc;
	}

	public byte[] getBytesArray() {
		return bytesArray;
	}
}
