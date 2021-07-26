package com.lisz.netty;

import io.netty.buffer.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import java.net.InetSocketAddress;

public class MyNetty {

	@Test
	public void myBytebuf() {
//		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8, 20);
		// pool
		//ByteBuf buf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
		ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(8, 20);
		print(buf);
		buf.writeBytes(new byte[]{1,2,3,4});
		print(buf);
		buf.writeBytes(new byte[]{1,2,3,4});
		print(buf);
		buf.writeBytes(new byte[]{1,2,3,4});
		print(buf);
		buf.writeBytes(new byte[]{1,2,3,4});
		print(buf);
		buf.writeBytes(new byte[]{1,2,3,4});
		print(buf);
//		buf.writeBytes(new byte[]{1,2,3,4}); // 报错，超过最大capacity.
//		print(buf);
	}

	private static void print(ByteBuf buf) {
		System.out.println("buf.isReadable() = " + buf.isReadable());           // 可不可读
		System.out.println("buf.readerIndex() = " + buf.readerIndex());         // 从哪里读
		System.out.println("buf.readableBytes() = " + buf.readableBytes());     // 可读字节数
		System.out.println("buf.isWritable() = " + buf.isWritable());           // 可不可写
		System.out.println("buf.writerIndex() = " + buf.writerIndex());         // 写位置
		System.out.println("buf.writableBytes() = " + buf.writableBytes());     // 可写字节数
		System.out.println("buf.capacity() = " + buf.capacity());               // 动态分配的，变化的
		System.out.println("buf.maxCapacity() = " + buf.maxCapacity());         // 设置好的
		System.out.println("buf.isDirect() = " + buf.isDirect());               // true为堆外内存
		System.out.println("--------------------------------------------");
	}

	/**
	 * 客户端
	 * 连接别人
	 * 1. 主动连接别人
	 * 2. 别人什么时候给我发？event selector
	 */
	@Test
	public void loopExecutor() throws Exception {
		// 先把group理解成一个线程池
		NioEventLoopGroup selector = new NioEventLoopGroup(2);
		selector.execute(() -> {
			while (true) {
				System.out.println("Hello world 001");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		selector.execute(() -> {
			while (true) {
				System.out.println("Hello world 002");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		System.in.read();
	}


	@Test
	public void clientMode() throws Exception {
		// Netty多路复用器
		NioEventLoopGroup thread = new NioEventLoopGroup();

		// 客户端模式
		NioSocketChannel client = new NioSocketChannel();

		// 读写都要用多路复用器：EventLoop. 客户端注册到多路复用器上
		// 否则报错：channel not registered to an event loop
		thread.register(client); // epoll_ctl(5, ADD, 3)

		// 响应式的，有了事件才会调用Handler
		ChannelPipeline pipeline = client.pipeline();
		pipeline.addLast(new MyInHandler());

		// reactor 异步的特征
		ChannelFuture connect = client.connect(new InetSocketAddress("192.168.1.99", 9090));

		// 连上了才往下走
		ChannelFuture sync = connect.sync();

		ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
		ChannelFuture send = client.writeAndFlush(buf);

		// 发送成功才能往下走
		send.sync();

		// 会阻塞在关闭等待上，以上步骤可能来回多次，是个长连接，服务端退出的时候会往下走
		sync.channel().closeFuture().sync();

		System.out.println("client over...");
	}
}
