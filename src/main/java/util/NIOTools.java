package util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class NIOTools
{
	private static CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
	
	private static CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	
	/**
	 * 显示客户端传过来的信息
	 * @param channel 连接的通道
	 * @throws Exception
	 */
	public synchronized static String byteBufferToString(SocketChannel channel) 
			throws Exception
	{
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		CharBuffer charBuffer = CharBuffer.allocate(1024);
	    buffer.clear();

	    StringBuffer text = new StringBuffer();
	    do
	    {
	    	buffer.flip();
	    	
	    	charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
	    	text.append(charBuffer.toString());
	    	buffer.clear();
	    }while(channel.read(buffer) > 0);

	    return text.toString();
	}
	
	/**
	 * 将字符串转换为ByteBuffer
	 * @param text 需要发送的字符串
	 * @return
	 * @throws Exception
	 */
	public synchronized static ByteBuffer stringToByteBuffer(String text) 
			throws Exception
	{
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		CharBuffer charBuffer = CharBuffer.allocate(1024);
		charBuffer = CharBuffer.wrap(text);
		buffer.clear();
		encoder.encode(charBuffer, buffer,true);
		buffer.flip();
		
		return buffer;
	}
}
