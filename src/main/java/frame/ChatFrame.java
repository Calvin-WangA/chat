package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ParserFriends;

/**
 * 获取好友信息，发送请求并且获得相应连接，之后开始聊天
 * 
 * @author CasparWang
 * 
 */
@SuppressWarnings("deprecation")
public class ChatFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 123495L;

	final JTextArea text = new JTextArea(6, 40);

	final JTextArea showText = new JTextArea(12, 40);

	private Socket socket;

	private PrintWriter dos = null;

	private BufferedReader dis = null;

	private String[] info;

	private String self;

	private int PORT;

	/**
	 * 构造函数中启动连接
	 */
	public ChatFrame(String[] info, int port) {
		this.PORT = port;
		this.info = info;

	}

	/**
	 * 连接好友
	 */
	public void connectFriend() {
		try {

			socket = new Socket(info[1], PORT);
			dos = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			dis = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			ParserFriends pf = new ParserFriends();
			self = pf.getUserByName("self")[1];
		} catch (Exception e) {
			System.out.println("好友不在线");
		}
	}

	/**
	 * 客户端窗口
	 */
	public void clientFrame() {

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
		JLabel label = new JLabel();
		label.setBackground(Color.blue);
		nPanel.add("North", showText);
		nPanel.add("South", label);

		JPanel cPanel = new JPanel();
		cPanel.setLayout(new BorderLayout(10, 10));
		cPanel.setSize(200, 100);
		text.setLineWrap(true);
		text.setAutoscrolls(true);
		cPanel.add(text);

		JPanel sPanel = new JPanel();
		sPanel.setLayout(new FlowLayout(2, 10, 10));
		sPanel.setSize(200, 100);
		JButton send = new JButton("发送");
		send.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				
				if (!info[3].equals("0")) {

					// 获取到文本内容，发送到服务端
					String t = text.getText();
					showText.append(self
							+ "  "
							+ new Date(System.currentTimeMillis())
									.toLocaleString() + "\n    " + t + "\n");
					send(t);
					// 每次点击发送后，清空文本框
					text.setText("");

				} else {
					showText.setText("当前好友不在线");
				}

			}

		});
		JButton reset = new JButton("重置");
		sPanel.add(send);
		sPanel.add(reset);

		content.add("North", nPanel);
		content.add("Center", cPanel);
		content.add("South", sPanel);
		pack();

		/**
		 * 重写窗体关闭事件，同时关闭socket连接
		 */
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {

				super.windowClosed(e);
				try {
					dis.close();
					dos.close();
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		setVisible(true);

		// 第一次判断是否在线，在线才创建连接
		if ((!info[3].equals("0"))) {
			connectFriend();
		}
		new Thread(new Runnable(){
			
			public void run() {
				try {
					receiver();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}).start();
		
	}

	/**
	 * 发送内容的方法
	 * 
	 * @param content
	 *            要发送的内容
	 */
	public void send(String content) {
		dos.println(content);
		dos.flush();
	}

	public void receiver() throws Exception {
		// 获得相应的服务端发的信息
		String value = "";
        if(null != dis)
        {
        	while (null != (value = dis.readLine())) {
    			if (!"".equals(value.trim())) {
    				showText.append(info[0] + "  "
    						+ new Date(System.currentTimeMillis()).toLocaleString()
    						+ "\n    " +value+ "\n");
    			}
    		}
        }
	}
}
