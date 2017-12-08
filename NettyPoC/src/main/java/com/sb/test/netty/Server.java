package com.sb.test.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

	private static final Logger log = LoggerFactory.getLogger(Server.class);

	private HelloServerHandler helloServerHandler = new HelloServerHandler();
	private int port;

	public Server(int port) {
		this.port = port;
	}

	public void start() throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(group);
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.localAddress(new InetSocketAddress("localhost", port));

			ChannelInitializer<SocketChannel> childHandler = new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					socketChannel.pipeline().addLast(helloServerHandler);
				}
			};
			serverBootstrap.childHandler(childHandler);

			ChannelFuture channelFuture = serverBootstrap.bind().sync();
			log.info("Server started on port: {}", port);

			channelFuture.channel().closeFuture().sync();
			
		} finally {
			group.shutdownGracefully().sync();
		}
	}

}
