package nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import util.NIOTools;

/**
 * 通过通道和选择器来实现监听
 * @author CasparWang
 *
 */
public class ServerChannelSelector
{

	private static final int SERVER_PORT = 5200;
	
	public static void main(String[] args) throws Exception 
	{
		new ServerChannelSelector().listener(args);
	}
	
	/**
	 * 监听连接的通道
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public void listener(String[] args) throws Exception
	{
		int port = SERVER_PORT;
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
	 * 对对应的连接通道进行注册
	 * @param server
	 * @param channel
	 * @param ops
	 */
	public void registChannel(Selector selector
			, SocketChannel channel, int ops) throws Exception
	{
		if(null == channel)
		{
			return ;
		}
		
		channel.configureBlocking(false);
		channel.register(selector, ops);
	}
	
	/**
	 * 从连接通道中读取数据
	 * @param key
	 * @throws Exception
	 */
	public void readDataFromChannel(SelectionKey key)
	    throws Exception
	{
		SocketChannel channel = (SocketChannel) key.channel();
		
	    String text = NIOTools.byteBufferToString(channel);
	    System.out.println(text);
	    channel.close();
	}
}
