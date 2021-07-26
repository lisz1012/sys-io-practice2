package com.lisz.netty;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class MyInHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client registered...");
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client active...");
	}

	// msg是读到的ByteBuf
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf buf = (ByteBuf) msg;
		//CharSequence str = buf.readCharSequence(buf.readableBytes(), CharsetUtil.UTF_8);
		// get不碰index指针，我们要制定起始位置
		CharSequence str = buf.getCharSequence(0, buf.readableBytes(), CharsetUtil.UTF_8);
		System.out.println("Received: " + str);
		ChannelFuture send = ctx.writeAndFlush(buf);
		send.sync();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("Read complete");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("Exception: " + cause);
	}
}
