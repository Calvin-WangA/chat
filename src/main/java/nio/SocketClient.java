package nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import util.NIOTools;

public class SocketClient {

	private static final int CLIENT_PORT = 50008;
	
	private static final String HOST = "localhost";
	
	private static final String text = "Hello! I'm Calvin Wang";
	
	public static void main(String[] args) throws Exception
	{
		ByteBuffer buffer = null;
		buffer = NIOTools.stringToByteBuffer(text);
		
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(HOST, CLIENT_PORT));
		
		while(!channel.finishConnect())
		{
			//doSomething()
		}

		channel.write(buffer);
		
		String reply = NIOTools.byteBufferToString(channel);
		System.out.println(reply);
		
		channel.close();
		
	}
	
}
