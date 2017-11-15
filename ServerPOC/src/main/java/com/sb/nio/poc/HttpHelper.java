package com.sb.nio.poc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.entity.EntityDeserializer;
import org.apache.http.impl.entity.LaxContentLengthStrategy;
import org.apache.http.impl.io.AbstractSessionInputBuffer;
import org.apache.http.impl.io.HttpRequestParser;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicLineParser;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHelper {

	private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

	public static HttpRequest create(final byte[] array) throws IOException, HttpException {
		SessionInputBuffer inputBuffer = new AbstractSessionInputBuffer() {
			{
				init(new ByteArrayInputStream(array), 10, new BasicHttpParams());
			}

			@Override
			public boolean isDataAvailable(int timeout) throws IOException {
				return false;
			}
		};
		HttpMessageParser parser = new HttpRequestParser(inputBuffer,
				new BasicLineParser(new ProtocolVersion("HTTP", 1, 1)), new DefaultHttpRequestFactory(),
				new BasicHttpParams());
		HttpMessage message = parser.parse();
		if (message instanceof BasicHttpEntityEnclosingRequest) {
			BasicHttpEntityEnclosingRequest request = (BasicHttpEntityEnclosingRequest) message;
			EntityDeserializer entityDeserializer = new EntityDeserializer(new LaxContentLengthStrategy());
			HttpEntity entity = entityDeserializer.deserialize(inputBuffer, message);
			request.setEntity(entity);
		}
		return (HttpRequest) message;
	}

	public static boolean isKeepAlive(HttpRequest request) {
		boolean keepAlive = false;

		Header[] headers = request.getHeaders(HTTP.CONN_DIRECTIVE);
		for (Header header : headers) {
			if (HTTP.CONN_KEEP_ALIVE.equalsIgnoreCase(header.getValue())) {
				keepAlive = true;
				break;
			}
			if (HTTP.CONN_CLOSE.equalsIgnoreCase(header.getValue())) {
				keepAlive = false;
				break;
			}
		}

		return keepAlive;
	}
}
