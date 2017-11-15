package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSimpleProcessor extends ProtocolProcessor {

	private static final String HTTP_REQUEST_END = "\r\n\r\n";

	private static final Logger log = LoggerFactory.getLogger(HttpSimpleProcessor.class);

	protected Response prepareResponse(final ByteBuffer readBuffer) {
		Response response = new Response();

		byte[] ba = new byte[readBuffer.position()];
		
		String s;
		if (readBuffer.isDirect()) {
			int pos = readBuffer.position();
			readBuffer.position(0);
			readBuffer.get(ba);
			readBuffer.position(pos);
			
			s = new String(ba);
		} else {
			s = new String(readBuffer.array(), 0, readBuffer.position());
		}
		
		// TODO think how better check if http request is complete
		if (!s.contains(HTTP_REQUEST_END)) {
			log.trace("Not complete input data: {}", s);
			return response;
		}

		log.debug("Incoming data: <<<\n{}>>>", s);
		log.debug(readBuffer.toString());

		boolean keepAlive = false;
		try {
			HttpRequest request = HttpHelper.create(ba);
			keepAlive = HttpHelper.isKeepAlive(request);
		} catch (IOException | HttpException e) {
			log.error("Error! Request: <<<\n{}>>>", s);
			log.error(e.getMessage(), e);
		}

		// - Preparing response
		String httpResponse = "HTTP/1.1 200 OK\r\n" + "Content-Length: 38\r\n" + "Content-Type: text/html\r\n"
				+ "\r\n" + "<html><body>Hello World!</body></html>";

		int index = s.indexOf(HTTP_REQUEST_END);
		
		response.readBytes = index + HTTP_REQUEST_END.length();
		response.body = httpResponse.getBytes();
		response.keepAlive = keepAlive;
		response.ready = true;

		return response;
	}
}

