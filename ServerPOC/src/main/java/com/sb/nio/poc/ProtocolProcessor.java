package com.sb.nio.poc;

import java.nio.ByteBuffer;

public interface ProtocolProcessor {
	void processData(ByteBuffer readBuffer);
}
