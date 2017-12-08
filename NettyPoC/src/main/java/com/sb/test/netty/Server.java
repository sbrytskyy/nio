package com.sb.test.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class Server {

	private static final Logger log = LoggerFactory.getLogger(Server.class);

//	private HelloServerHandler helloServerHandler = new HelloServerHandler();
	private HttpHandler httpHandler = new HttpHandler();
	private int port;

	private final int nThreads = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
	private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(nThreads);

	public Server(int port) {
		this.port = port;
	}

	public void start() throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup(nThreads, executor);

		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(group);
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.localAddress(new InetSocketAddress("localhost", port));

			ChannelInitializer<SocketChannel> childHandler = new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					socketChannel.pipeline().addLast("codec", new HttpServerCodec());
					socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
					socketChannel.pipeline().addLast("request", httpHandler);
//					socketChannel.pipeline().addLast("echo", helloServerHandler);
				}
			};
			serverBootstrap.childHandler(childHandler);
			serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);

			ChannelFuture channelFuture = serverBootstrap.bind().sync();
			log.info("Server started on port: {}", port);

			channelFuture.channel().closeFuture().sync();

		} finally {
			group.shutdownGracefully().sync();
		}
	}

}
