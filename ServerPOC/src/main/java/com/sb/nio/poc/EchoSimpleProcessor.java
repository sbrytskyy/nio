package com.sb.nio.poc;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoSimpleProcessor extends ProtocolProcessor {

	private static final Logger log = LoggerFactory.getLogger(EchoSimpleProcessor.class);

	protected Response prepareResponse(final ByteBuffer readBuffer) {
		Response response = new Response();

		byte[] array = readBuffer.array();
		String s = new String(readBuffer.array(), 0, readBuffer.position());
		log.debug("Incoming message: <<<{}>>>", s);
		
		response.readBytes = s.length();
		response.body = new byte[s.length()];
		System.arraycopy(array, 0, response.body, 0, s.length());
		response.keepAlive = false;
		response.ready = true;

		return response;
	}
}

