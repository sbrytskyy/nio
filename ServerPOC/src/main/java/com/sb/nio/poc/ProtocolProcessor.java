package com.sb.nio.poc;

import java.nio.channels.Selector;
import java.util.Queue;

public interface ProtocolProcessor extends Runnable {
	
	void init(Queue<Message> outboundMessageQueue, Selector selector);

	void processData(IncomingData data);
}
