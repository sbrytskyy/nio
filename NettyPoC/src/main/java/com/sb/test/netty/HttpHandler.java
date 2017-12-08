package com.sb.test.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@ChannelHandler.Sharable
public class HttpHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			final FullHttpRequest request = (FullHttpRequest) msg;

			final String responseMessage = "Hello from Netty!";

			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					Unpooled.copiedBuffer(responseMessage.getBytes()));

			if (HttpHeaders.isKeepAlive(request)) {
				response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());

			ctx.writeAndFlush(response);
		} else {
			super.channelRead(ctx, msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR,
				Unpooled.copiedBuffer(cause.getMessage().getBytes())));
	}
}
