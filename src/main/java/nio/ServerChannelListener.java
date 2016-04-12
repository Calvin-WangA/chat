package nio;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import util.NIOTools;

public class ServerChannelListener
{

	private static final int SERVER_PORT = 50008;
	
	public static void main(String[] args) throws Exception
	{
		int port = SERVER_PORT;
		
		if(args.length > 0)
		{
			port = Integer.parseInt(args[0]);
		}
		serverListener(port);
	}
	
	/**
	 * 监听端口方法
	 * @param port
	 * @throws Exception
	 */
	public static void serverListener(int port) throws Exception
	{
		SocketChannel channel = null;
		ServerSocketChannel server = ServerSocketChannel.open();
		ServerSocket serverSocket = server.socket();
		//绑定端口号
		serverSocket.bind(new InetSocketAddress(port));
		server.configureBlocking(false);//必须配置非阻塞
		
		while(true)
		{
			channel = server.accept();
			if(null != channel)
			{
				System.out.println(NIOTools.byteBufferToString(channel));
			}
		}	
		
	}
}
