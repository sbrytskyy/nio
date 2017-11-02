package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

public interface ProtocolProcessor {
	void processData(ByteBuffer readBuffer, SocketContainer sc) throws IOException;

	void setMessageQueue(Queue<Message> outboundMessageQueue);
}
