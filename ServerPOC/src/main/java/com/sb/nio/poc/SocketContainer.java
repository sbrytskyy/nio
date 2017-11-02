package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketContainer {

	private SocketChannel channel;
	private boolean endOfStreamReached;

	public SocketContainer(SocketChannel channel) {
		this.channel = channel;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public boolean isEndOfStreamReached() {
		return endOfStreamReached;
	}

	public int read(ByteBuffer byteBuffer) throws IOException {
		int bytesRead = channel.read(byteBuffer);
		int totalBytesRead = bytesRead;

		while (bytesRead > 0) {
			bytesRead = channel.read(byteBuffer);
			totalBytesRead += bytesRead;
		}
		if (bytesRead == -1) {
			endOfStreamReached = true;
		}

		return totalBytesRead;
	}

	public int write(ByteBuffer byteBuffer) throws IOException {
		int totalWritten = 0;
		
		while(byteBuffer.hasRemaining()) {
			int written = channel.write(byteBuffer);
			totalWritten += written;
		}
		
		return totalWritten;
	}
}
