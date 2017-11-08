package com.sb.nio.poc;

public interface IDataProcessor extends Runnable {

	void setMessageListener(MessageListener listener);
	
	void processData(IncomingData data);
}
