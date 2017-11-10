package com.sb.nio.poc;

public class ProtocolProcessorFactory {
	public static ProtocolProcessor getProtocolProcessor(Protocol protocol) {
		ProtocolProcessor p = null;
		if (protocol.equals(Protocol.HTTP)) {
			p = new HttpSimpleProcessor();
		}

		return p;
	}
}
