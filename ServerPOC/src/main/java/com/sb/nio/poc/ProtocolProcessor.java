package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.util.Queue;

public interface ProtocolProcessor {
	void processData(ByteBuffer readBuffer, SocketContainer sc) throws IOException;

	void init(Queue<Message> outboundMessageQueue, Selector selector);
}
