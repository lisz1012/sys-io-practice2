package com.lisz.netty;

import io.netty.buffer.*;
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
//		buf.writeBytes(new byte[]{1,2,3,4});
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
	public void clientMode() {
		// NioEventLoopGroup thread = new NioEventLoopGroup();
		// 客户端模式
		NioSocketChannel client = new NioSocketChannel();
		client.connect(new InetSocketAddress("192.168.1.253", 9090));
		ByteBuf buf = Unpooled.copiedBuffer("hello server".getBytes());
		client.writeAndFlush(buf);

		System.out.println("client over...");
	}
}
