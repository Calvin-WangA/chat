package nio;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import util.NIOTools;
import util.ParserUsers;

public class ServerChannelSelectorThreadPool extends ServerChannelSelector
{
	private final Logger log = (Logger) LoggerFactory
			.getLogger(ServerChannelSelectorThreadPool.class);
	private static int MAX_THREADS = 5;

	private static ThreadPool pool = null;

	public static void main(String[] args) throws Exception
	{
		MAX_THREADS = Runtime.getRuntime().availableProcessors();
		ServerChannelSelectorThreadPool server = new ServerChannelSelectorThreadPool();
		pool = server.new ThreadPool(MAX_THREADS);
		new ServerChannelSelectorThreadPool().listener(args);
	}

	/**
	 * 读取数据
	 */
	@Override
	public void readDataFromChannel(SelectionKey key) throws Exception
	{
		WorkerThread thread = pool.getWorker();
		if (null == thread)
		{
			return;
		}

		thread.serviceChannel(key);
	}

	private class ThreadPool
	{
		List<WorkerThread> idle = new LinkedList<WorkerThread>();

		/**
		 * 初始化线程池中的线程
		 * 
		 * @param poolSize
		 */
		ThreadPool(int poolSize)
		{
			WorkerThread thread = null;
			for (int i = 0; i < poolSize; i++)
			{
				thread = new WorkerThread(this);
				thread.setName("server listener thread" + i);
				thread.start();
				idle.add(thread);
			}
		}

		/**
		 * 得到空闲线程
		 * 
		 * @return
		 */
		WorkerThread getWorker()
		{
			WorkerThread thread = null;
			synchronized (idle)
			{
				if (idle.size() > 0)
				{
					thread = (WorkerThread) idle.remove(0);
				}
			}

			return thread;
		}

		/**
		 * 将工作完的线程放入线程池中
		 * 
		 * @param thread
		 */
		void returnWorkerThread(WorkerThread thread)
		{
			synchronized (idle)
			{
				idle.add(thread);
			}
		}
	}

	/**
	 * 工作线程
	 * 
	 * @author CasparWang
	 *
	 */
	private class WorkerThread extends Thread
	{
		private ThreadPool pool;

		private SelectionKey key;

		WorkerThread(ThreadPool pool)
		{
			this.pool = pool;
		}

		@Override
		public synchronized void run()
		{
			log.info(this.getName() + "is ready");
			while (true)
			{
				try
				{
					this.wait();
				} catch (InterruptedException e)
				{
					e.printStackTrace();
					this.isInterrupted();
				}

				if (null == key)
				{
					continue;
				}
				log.info(this.getName() + "has been awakened");

				try
				{
					drainChannel(key);
				} catch (Exception e)
				{
					log.info("Caught '" + e + "' closing channel");
					try
					{
						key.channel().close();
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}

				key = null;

				this.pool.returnWorkerThread(this);
			}
		}

		/**
		 * 通过该方法初始化通道
		 * 
		 * @param key
		 * @throws Exception
		 */
		synchronized void serviceChannel(SelectionKey key) throws Exception
		{
			this.key = key;
			// 对该操作进行取反
			key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
			// 通知其他线程
			this.notify();
		}

		/**
		 * 通过该方法处理连接的通道
		 * 
		 * @param key
		 * @throws Exception
		 */
		void drainChannel(SelectionKey key) throws Exception
		{
			SocketChannel channel = (SocketChannel) key.channel();
			Socket socket = channel.socket();
			// 用于处理更新用户信息，并查询用户好友信息返回给用户
			String userInfo = NIOTools.byteBufferToString(channel);
			String[] values = userInfo.split(":");
			String[] user = new String[4];
			ParserUsers pu = new ParserUsers();
			// 如果是这样，表名用户退出
			if ("exit".equals(values[1]))
			{
				user[3] = "0";
				log.info(values[0] + "用户退出软件");
			} else
			{
				user[3] = "1";
				log.info(values[0] + "用户登录软件");
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
			if (!"exit".equals(values[1]))
			{
				// 转换为gson格式的字符串
				Gson gson = new Gson();
				String users = gson.toJson(map);
				// 返回给用户信息
				channel.write(NIOTools.stringToByteBuffer(users));
			}

			channel.close();
			// 重置通道感兴趣的操作
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			// 轮询selector时，key再被激活
			key.selector().wakeup();
		}
		
		/**
		 * 通知上下线用户的好友，该用户上下线的情况
		 * 
		 * @param map
		 *            所以的好友都在里面
		 * @param user
		 *            用户本人信息
		 */
		public synchronized void notifyFriends(Map<String, String[]> map, String[] user)
		{
			SocketChannel notify = null;
			String name = "";
			String flag = "";
			for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
			{
				name = iter.next();
				String[] info = map.get(name);
				// 当该用户好友不在线时，不进行上下线的通知
				if ((user[0].equals(name)) || info[2].equals("0")
						|| "self".equals(name))
				{
					continue;
				}

				try
				{
					notify = SocketChannel.open();
					notify.socket().bind(new InetSocketAddress(info[0], 12345));
					while(!notify.finishConnect())
					{
						//doSomething
					}
					notify.write(NIOTools.stringToByteBuffer(user[0] 
							+ ":" + user[1] + ":" + user[2] + ":" + user[3]));
					flag = NIOTools.byteBufferToString(notify);
					if("get".equals(flag))
					{
						notify.close();
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
