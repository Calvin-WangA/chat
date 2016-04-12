package util;

import java.util.Date;

public class Tools
{
	/**
	 * 格式化发送或接收的字符串
	 * @param content
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getChatFormatString(String user,String content)
	{
		StringBuffer buffer = new StringBuffer(user);
		
		buffer.append("  ");
		buffer.append(new Date(System.currentTimeMillis()).toLocaleString());
		buffer.append("\n    ");
		buffer.append(content);
		buffer.append("\n");
		
		return buffer.toString();
	}

}
