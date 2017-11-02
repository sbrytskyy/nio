package com.sb.nio.poc;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleProcessor implements ProtocolProcessor {

	private static final Logger log = LoggerFactory.getLogger(SimpleProcessor.class);

	@Override
	public void processData(ByteBuffer readBuffer) {
		StringBuilder sb = new StringBuilder();
		while (readBuffer.hasRemaining()) {
			sb.append((char) readBuffer.get()); // read 1 byte at a time
		}
		log.info("Incoming data: <<<\n{}>>>", sb.toString());
	}

}
