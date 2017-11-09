package com.sb.nio.poc;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;

public interface IDataProcessor extends Runnable {

	void setMessageListener(MessageListener listener);
	
	void setSocketCachedData(Map<Socket, ByteBuffer> socketCachedData);
	
	void processData(Socket socket);
}
