package com.sb.nio.poc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpSimpleProcessor extends ProtocolProcessor {

	private static final String HTTP_REQUEST_END = "\r\n\r\n";

	public HttpSimpleProcessor(IncomingData data, DataProcessorCallback callback) {
		super(data, callback);
	}

	private static final Logger log = LoggerFactory.getLogger(HttpSimpleProcessor.class);

	protected Response prepareResponse(final ByteBuffer readBuffer) {
		Response response = new Response();

		String s = new String(readBuffer.array());
		
		// TODO think how better check if http request is complete
		if (!s.contains(HTTP_REQUEST_END)) {
			log.trace("Not complete input data: {}", s);
			return response;
		}

		log.debug("Incoming data: <<<\n{}>>>", s);
		log.debug(readBuffer.toString());
		log.debug(Arrays.toString(readBuffer.array()));

		boolean keepAlive = false;
		try {
			HttpRequest request = HttpHelper.create(readBuffer);
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

