package com.lisz.netty;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;


public class MyAcceptHandler extends ChannelInboundHandlerAdapter {

	private EventLoopGroup selector;

	private ChannelHandler handler;

	public MyAcceptHandler(EventLoopGroup selector, ChannelHandler handler) {
		this.selector = selector;
		this.handler = handler;
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("server registered...");
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// listen 的socket，只能accept读不到消息
		// 客户端的 socket。就可以读到数据
		// 在不同的通道上，Read最开始读到的东西不一样
		//accept 我怎么没调呢？接收完了之后把客户端直接传进来了
		SocketChannel client = (SocketChannel)msg;
		// 注册
		selector.register((client));
		// 响应式的
		client.pipeline().addLast(handler);
	}
}
