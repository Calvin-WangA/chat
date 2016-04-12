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
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import util.NIOTools;
import util.ParserFriends;
import util.ThreadPool;

/**
 * 获取好友信息，发送请求并且获得相应连接，之后开始聊天
 * 
 * @author CasparWang
 * 
 */
@SuppressWarnings("deprecation")
public class ChatFrame extends JFrame
{
	private static final long serialVersionUID = 123495L;

	final JTextArea text = new JTextArea(6, 40);

	final JTextArea showText = new JTextArea(12, 40);

	private SocketChannel channel;
	
	private ExecutorService pool;

	private String[] info;

	private String self;

	private int PORT;

	/**
	 * 构造函数中启动连接
	 */
	public ChatFrame(String[] info, int port)
	{
		this.PORT = port;
		this.info = info;
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
	 * 客户端窗口
	 */
	public synchronized void clientFrame() throws Exception
	{

		setTitle(info[0]);
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
		
		connectReceive();
	}
	

	/**
	 * 连接好友
	 */
	public void connectFriend()
	{
		try
		{
			channel = SocketChannel.open(new InetSocketAddress(info[1], PORT));
			channel.configureBlocking(false);
			while(!channel.finishConnect())
			{
				//nothing
			}
			ParserFriends pf = new ParserFriends();
			self = pf.getUserByName("self")[1];
		} catch (Exception e)
		{
			System.out.println("好友不在线");
		}
	}

	/**
	 * 发送内容的方法
	 * 
	 * @param content
	 *            要发送的内容
	 */
	public void sendData()
	{
		if (!info[3].equals("0"))
		{
			
			// 获取到文本内容，发送到服务端
			String t = text.getText();
			showText.append(self + "  " 
			    + new Date(System.currentTimeMillis()).toLocaleString() 
			    + "\n    " + t + "\n");
			showText.setCaretPosition(showText.getText().length());
			try
			{
				channel.write(NIOTools.stringToByteBuffer(t));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			
			// 每次点击发送后，清空文本框
			text.setText("");

		} else
		{
			showText.setText("当前好友不在线");
		}

	}

	/**
	 * 连接指定好友，并开始接收用户发送的信息
	 * @throws Exception
	 */
	public synchronized void connectReceive() throws Exception
	{
		pool = ThreadPool.newThreadPool(Runtime
				.getRuntime().availableProcessors());
		// 第一次判断是否在线，在线才创建连接
		if ((info[3].equals("0"))
				)
		{
			return ;
		}
		connectFriend();
		pool.execute(new Runnable(){
			public void run()
			{
				try
				{
					while(true)
					{
						// 获得相应的服务端发的信息
						String value = NIOTools.byteBufferToString(channel);
						if (!"".equals(value.trim()))
						{
							showText.append(info[0] + "  " 
						    + new Date(System.currentTimeMillis())
						         .toLocaleString()
							+ "\n    " + value + "\n");
							showText.setCaretPosition(showText.getText().length());
						}
					}
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
	   });
	}
}
