package com.lisz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// 参见ProcessOn的类图，IO这一块，是很多分布式框架的基础
// 他们大都会用到这方面的知识，尤其是NIO
public class SocketMultiplexingThreads {

	private ServerSocketChannel server;
	private Selector selector1;
	private Selector selector2;
	private Selector selector3;
	private int port = 9090;


	public void initServer() {
		try {
			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(new InetSocketAddress(port));
			selector1 = Selector.open();
			selector2 = Selector.open();
			selector3 = Selector.open();
			// selector1的用途生来就不一样
			server.register(selector1, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public static void main(String[] args) {
		SocketMultiplexingThreads service = new SocketMultiplexingThreads();
		service.initServer();
		Thread t1 = new NioThread(service.selector1, 2);
		Thread t2 = new NioThread(service.selector2);
		Thread t3 = new NioThread(service.selector3);

		t1.start();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		t2.start();
		t3.start();

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}



class NioThread extends Thread {
	private Selector selector;

	private static int selectors = 0;

	private static volatile LinkedBlockingQueue<SocketChannel>[] queues;

	private int id = 0;

	private static AtomicInteger idx = new AtomicInteger(0);


	public NioThread(Selector selector, int n) {
		this.selector = selector;
		selectors = n;
		queues = new LinkedBlockingQueue[selectors];
		for (int i = 0; i < n; i++) {
			queues[i] = new LinkedBlockingQueue<SocketChannel>();
		}
	}


	public NioThread(Selector selector) {
		this.selector = selector;
		id = idx.getAndIncrement() % selectors;
	}



	@Override
	public void run() {
		try {
			while (true) {
				while (selector.select(10) > 0) {
					Set<SelectionKey> selectionKeys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = selectionKeys.iterator();
					while (iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();
						if (key.isAcceptable()) {
							handleAccept(key);
						} else if (key.isReadable()) {
							handleRead(key);
						}
					}
				}
				if (!queues[id].isEmpty()) { // 出队 + 注册读事件
					SocketChannel client = queues[id].take();
					ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
					client.register(selector, SelectionKey.OP_READ, buffer);
					System.out.println("-------------------------------------------");
					System.out.println("新客户端：" + client.socket().getPort()+"分配到："+ (id));
					System.out.println("-------------------------------------------");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	// accept得到SocketChannel + 入队
	private void handleAccept(SelectionKey key) {
		try {
			ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
			SocketChannel client = ssc.accept();
			client.configureBlocking(false);
			int num = idx.getAndIncrement() % selectors;
			queues[num].offer(client);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// 读数据，这里可能涉及到分手
	private void handleRead(SelectionKey key) {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		buffer.clear();
		int read = 0;
		try {
			while (true) {
				read = client.read(buffer);
				if (read > 0) { // 正在读，读到了文件中间
					buffer.flip();
					while (buffer.hasRemaining()) {
						client.write(buffer);
					}
					buffer.clear();
				} else if (read == 0) { // 正常读完了
					break;
				} else { // 对方关闭
					client.close();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
