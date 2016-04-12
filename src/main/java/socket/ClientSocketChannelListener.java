package socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import frame.ServerFrame;
import nio.ServerChannelSelector;
import util.NIOTools;
import util.ParserFriends;

public class ClientSocketChannelListener extends ServerChannelSelector
{
	private final Logger log = (Logger) LoggerFactory
			.getLogger(ClientSocketChannelListener.class);
    private static final int PORT = 12345;// 监听的端口号
    
	private static int MAX_THREADS = 5;
	
	private static Map<String,ServerFrame> frames = null;

	private static UpdatePool pool = null;

	public static void main(String[] args) throws Exception
	{
		MAX_THREADS = Runtime.getRuntime().availableProcessors();
		ClientSocketChannelListener server = new ClientSocketChannelListener();
		pool = server.new UpdatePool(MAX_THREADS);
		frames = new HashMap<String, ServerFrame>();
		new ClientSocketChannelListener().listener(args);
	}
	
	@SuppressWarnings("rawtypes")
	public void listener(String[] args) throws Exception
	{
		int port = PORT;
		if(args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		}
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverChannel.configureBlocking(false);
		
		Selector selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		int count = 0;
		SelectionKey key = null;
		while(true)
		{
			count = selector.select();
			if(count == 0)
			{
				continue;
			}
			
			Iterator iter = selector.selectedKeys().iterator();
			while(iter.hasNext())
			{
				key = (SelectionKey)iter.next();
				if(key.isAcceptable())
				{
					ServerSocketChannel server = (ServerSocketChannel) 
							key.channel();
					SocketChannel channel = server.accept();
					registChannel(selector,channel, SelectionKey.OP_READ);
				}
				
				if(key.isReadable())
				{
					readDataFromChannel(key);
				}
				
				//连接已被处理，所以将其移除
				iter.remove();
			}
		}
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

	private class UpdatePool
	{
		List<WorkerThread> idle = new LinkedList<WorkerThread>();

		/**
		 * 初始化线程池中的线程
		 * 
		 * @param poolSize
		 */
		UpdatePool(int poolSize)
		{
			WorkerThread thread = null;
			for (int i = 0; i < poolSize; i++)
			{
				thread = new WorkerThread(this);
				thread.setName("client listener thread" + i);
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
		private UpdatePool pool;

		private SelectionKey key;
		
		private ServerFrame server;

		WorkerThread(UpdatePool pool)
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
		@SuppressWarnings("deprecation")
		void drainChannel(SelectionKey key) throws Exception
		{
			SocketChannel channel = (SocketChannel) key.channel();
			channel.configureBlocking(false);
			
			try {
		
				String[] info = NIOTools.byteBufferToString(channel)
						.trim().split(":");
				if (info.length > 1) {
					// 更新friends.xml
					ParserFriends update = new ParserFriends();
					update.updateFriend(info);
					channel.write(NIOTools.stringToByteBuffer("get"));
					//channel.close();
					// 重置通道感兴趣的操作
					key.interestOps(key.interestOps() | SelectionKey.OP_READ);
					// 轮询selector时，key再被激活
					key.selector().wakeup();
					String addr = InetAddress.getLocalHost().getHostAddress();
					// 当前用户下线时关闭对应的监听器
					if (info[3].equals("0") && info[1].equals(addr)) {
						System.exit(0);
					}
				} else {
					
					server = frames.get(info[0]);
					if(null == server)
					{
						server = new ServerFrame(channel);
						server.serverFrame(info[0]);
						frames.put(info[0], server);
					}
					else
					{
						//server.disable();
						server.setChannel(channel);
						server.show();
						//server.serverFrame(info[0]);
					}
					
				}

			} catch (Exception e) {
				channel.close();
				System.out.println("服务器 异常： " + e.getMessage());
			}
		}
	}

}
