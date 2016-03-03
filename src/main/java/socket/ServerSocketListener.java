package socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import util.ParserUsers;

import com.google.gson.Gson;

/**
 * 服务器监听程序，用于更新用户上下线状态并进行通知
 * 
 * @author CasparWang
 * 
 */
public class ServerSocketListener {

	private final int PORT = 5200;

	private ExecutorService executor;

	public ServerSocketListener() {
		// 创建线程池，使用自定义的执行框架执行线程
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 100, 60L,
				TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(80),
				new ThreadPoolExecutor.CallerRunsPolicy());
		threadPool.allowCoreThreadTimeOut(true);
		executor = threadPool;
	}

	public static void main(String[] args) {
		ServerSocketListener listener = new ServerSocketListener();
		listener.init();
	}

	public synchronized void init() {

		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			while (true) {
				// 一旦有堵塞， 则表示服务器与客户端获得了连接
				Socket client = serverSocket.accept();
				// 处理这次连接
				executor.execute(new HandlerThread(client));
			}
		} catch (Exception e) {
			System.out.println("服务器异常： " + e.getMessage());
		}
	}

	/**
	 * 启动一个线程去执行所有要执行的操作
	 * 
	 * @author CasparWang
	 * 
	 */
	private class HandlerThread implements Runnable {
		private Socket socket;

		public HandlerThread(Socket client) {
			socket = client;

		}

		public void run() {
			InputStreamReader isr = null;
			BufferedReader reader = null;
			OutputStreamWriter osr = null;
			BufferedWriter writer = null;
			try {
				// 用于处理更新用户信息，并查询用户好友信息返回给用户
				isr = new InputStreamReader(socket.getInputStream());
				reader = new BufferedReader(isr);
				String userInfo = reader.readLine();
				String[] values = userInfo.split(":");
				String[] user = new String[4];
				ParserUsers pu = new ParserUsers();
				// 如果是这样，表名用户退出
				if ("exit".equals(values[1])) {
					user[3] = "0";
				} else {
					user[3] = "1";
				}
				// 更新用户信息
				user[0] = values[0];
				user[1] = socket.getInetAddress().getHostAddress();
				user[2] = "" + socket.getPort();
				pu.modifyUserInfo(user);
				// 查询
				Map<String, String[]> map = pu.parserXml();
				// 通知用户好友
				notifyFriends(map, user);
				// 登录的时候才回传数据
				if (!"exit".equals(values[1])) {
					// 转换为gson格式的字符串
					Gson gson = new Gson();
					String users = gson.toJson(map);
					// 返回给用户信息
					osr = new OutputStreamWriter(socket.getOutputStream());
					writer = new BufferedWriter(osr);
					writer.write(users);
					writer.flush();
				}

			} catch (Exception e) {
				System.out.println("服务器 run 异常： " + e.getMessage());
			} finally {
				if (null != socket) {
					try {

						socket.close();
					} catch (IOException e) {
						System.out.println("服务器关闭异常");
					}
				}
			}
		}

		/**
		 * 通知上下线用户的好友，该用户上下线的情况
		 * 
		 * @param map
		 *            所以的好友都在里面
		 * @param user
		 *            用户本人信息
		 */
		public void notifyFriends(Map<String, String[]> map, String[] user) {
			Socket notify = null;
			PrintWriter writer = null;
			String name = "";
			for (Iterator<String> iter = map.keySet().iterator(); iter
					.hasNext();) {
				name = iter.next();
				String[] info = map.get(name);
				// 当该用户好友不在线时，不进行上下线的通知
				if ((!user[0].equals(name)) && info[2].equals("0")) {
					continue;
				}

				try {
					notify = new Socket(info[0], 12345);
					writer = new PrintWriter(notify.getOutputStream());
					writer.write(user[0] + ":" + user[1] + ":" + user[2] + ":"
							+ user[3]);
					writer.flush();
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
