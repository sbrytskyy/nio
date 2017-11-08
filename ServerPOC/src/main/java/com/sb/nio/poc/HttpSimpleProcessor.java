package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSimpleProcessor extends ProtocolProcessor {

	public HttpSimpleProcessor(IncomingData data, MessageListener listener) {
		super(data, listener);
	}

	private static final Logger log = LoggerFactory.getLogger(HttpSimpleProcessor.class);

	protected boolean prepareResponse(final ByteBuffer readBuffer, ByteBuffer writeBuffer) {
		String s = new String(readBuffer.array());
		log.debug("Incoming data: <<<\n{}>>>", s);

		// - Preparing response
		String httpResponse = "HTTP/1.1 200 OK\r\n" + "Content-Length: 38\r\n" + "Content-Type: text/html\r\n"
				+ "\r\n" + "<html><body>Hello World!</body></html>";

		byte[] httpResponseBytes = httpResponse.getBytes();

		writeBuffer.clear();
		writeBuffer.put(httpResponseBytes);
		writeBuffer.flip();
		
		return true;
	}

	protected boolean isKeepAlive(ByteBuffer readBuffer) {
		boolean keepAlive = false;
		try {
			HttpRequest request = HttpHelper.create(readBuffer);
			keepAlive = HttpHelper.isKeepAlive(request);
		} catch (IOException | HttpException e) {
			log.error(e.getMessage(), e);
		}
		return keepAlive;
	}
}

