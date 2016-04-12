package socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import util.ParserFriends;
import frame.ServerFrame;

/**
 * 启动服务器监听端口
 * 
 * @author CasparWang
 * 
 */
public class SocketListener {

	public static final int PORT = 12345;// 监听的端口号

	private ExecutorService executor;
	
	public static void main(String[] args) {
		SocketListener listener = new SocketListener();
		//启动sokect监听
		listener.init();
	}

	public void init() {
		// 创建线程池，使用自定义的执行框架执行线程
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(1, 20, 60L,
				TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(2),
				new ThreadPoolExecutor.CallerRunsPolicy());
		threadPool.allowCoreThreadTimeOut(true);
		executor = threadPool;
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				// 一旦有堵塞， 则表示服务器与客户端获得了连接
				Socket client = serverSocket.accept();
				// 处理这次连接
				executor.execute((new HandlerThread(client)));
			}
		} catch (Exception e) {
			System.out.println("服务器异常： " + e.getMessage());
		}
	}

	private class HandlerThread implements Runnable {
		private Socket socket;

		private ServerFrame server;

		private BufferedReader reader;

		public HandlerThread(Socket client) {
			socket = client;

		}

		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String[] info = reader.readLine().trim().split(":");
				if (info.length > 1) {
					// 更新friends.xml
					ParserFriends update = new ParserFriends();
					update.updateFriend(info);
					socket.close();
					String addr = InetAddress.getLocalHost().getHostAddress();
					// 当前用户下线时关闭对应的监听器
					if (info[3].equals("0") && info[1].equals(addr)) {
						System.exit(0);
					}
				} else {
					//server = new ServerFrame(socket);
					server.serverFrame(info[0]);
				}

			} catch (Exception e) {
				System.out.println("服务器 异常： " + e.getMessage());
			} finally {
				/*
				 * if (socket != null) { try { socket.close(); } catch
				 * (Exception e) { socket = null;
				 * System.out.println("服务端 finally 异常：" + e.getMessage()); } }
				 */
			}
		}
	}

}
