package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.NIOTools;
import util.ParserFriends;
import util.Tools;

public class ServerFrame extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 12323352L;

	private SocketChannel channel = null;

	final JTextArea text = new JTextArea(6, 40);

	final JTextArea showText = new JTextArea(12, 40);

	private boolean flag = true;

	private String[] info;

	private String self;

	public ServerFrame(final SocketChannel channel)
	{
		this.channel = channel;
		ParserFriends pf = new ParserFriends();
		this.info = pf.getUserByIp(channel.socket()
				.getInetAddress().getHostAddress());
		// 本机ip地址的获取方式InetAddress.getLocalHost().getHostAddress()
		// 监听到的对方的ip获取方式socket.getInetAddress().getHostAddress()
		if (null == info)
		{
			info = new String[] { "好友" };
		}
		self = pf.getUserByName("self")[1];
		/**
		 * 重写窗体关闭事件，同时关闭socket连接
		 */
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{

				super.windowClosed(e);
				try
				{
					channel.close();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
	}

	/**
	 * 服务器窗口
	 */
	public void serverFrame(String info) throws Exception
	{

		if (null == this.info)
		{
			setTitle("好友");
		} else
		{
			setTitle(this.info[0]);
		}

		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400, 500);
		// 窗体居中
		setLocationRelativeTo(null);
		Container content = getContentPane();
		content.setBackground(Color.white);
		content.setLayout(new BorderLayout(10, 10));

		JPanel nPanel = new JPanel();
		nPanel.setLayout(new BorderLayout(10, 10));
		nPanel.setSize(200, 100);

		showText.setLineWrap(true);
		showText.setAutoscrolls(true);
		showText.setEditable(false);
		JScrollPane scrollShow = new JScrollPane(showText);
		scrollShow.setBorder(BorderFactory.createEmptyBorder());
		JLabel label = new JLabel();
		label.setBackground(Color.blue);
		nPanel.add("North", scrollShow);
		nPanel.add("South", label);

		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BorderLayout(10, 10));
		cPanel.setSize(200, 100);
		text.setLineWrap(true);
		text.setAutoscrolls(true);
		JScrollPane scrollInput = new JScrollPane(text);
		scrollInput.setBorder(BorderFactory.createEmptyBorder());
		cPanel.add(scrollInput);

		JPanel sPanel = new JPanel();
		sPanel.setLayout(new FlowLayout(2, 10, 10));
		sPanel.setSize(200, 100);
		JButton send = new JButton("发送");
		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendData();
			}
		});
		JButton reset = new JButton("重置");
		sPanel.add(send);
		sPanel.add(reset);
		content.add("North", nPanel);
		content.add("Center", cPanel);
		content.add("South", sPanel);

		pack();
		setVisible(true);
		
		receiver(info);
	}

	/**
	 * 向客户端写数据
	 * 
	 * @param content
	 */
	public void sendData()
	{
		// 获取到文本内容，发送到服务端
		String t = text.getText();
		try
		{
			channel.write(NIOTools.stringToByteBuffer(t));
			// 每次点击发送后，清空文本框
			text.setText("");
			showText.append(Tools.getChatFormatString(self,t));
			showText.setCaretPosition(showText.getText().length());
			// 将本身的十六进制字符串转换成十六进制字节数组,再发送出去
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 接受数据并写到框架中
	 * 
	 * @return
	 */
	public void receiver(String info) throws Exception
	{
		// 设置发送的第一条消息
		showText.append(Tools.getChatFormatString(this.info[0], info));
		while (flag)
		{
			try
			{
				String value = NIOTools.byteBufferToString(channel);
				if (!"".equals(value.trim()))
				{
					showText.append(Tools.getChatFormatString(this.info[0],value));
					showText.setCaretPosition(showText.getText().length());
				}
			} catch (Exception e1)
			{
				flag = false;
				e1.printStackTrace();
		    }
		}
		
		return;
	}
	
	public void setChannel(SocketChannel channel)
	{
		this.channel = channel;
	}
}
