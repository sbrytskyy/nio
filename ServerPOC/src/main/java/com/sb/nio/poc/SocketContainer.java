package com.sb.nio.poc;

import java.nio.channels.SocketChannel;

public class SocketContainer {

	private SocketChannel channel;

	public SocketContainer(SocketChannel channel) {
		this.channel = channel;
	}

}
