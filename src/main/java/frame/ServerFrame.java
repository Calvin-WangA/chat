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
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import util.ParserFriends;

@SuppressWarnings("deprecation")
public class ServerFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 12323352L;

	private Socket socket = null;

	private PrintWriter dos = null;

	private BufferedReader in = null;

	final JTextArea text = new JTextArea(6, 40);

	final JTextArea showText = new JTextArea(12, 40);

	private boolean flag = true;

	private String[] info;
	
	private String self;
	public ServerFrame(Socket socket)
	{
		this.socket = socket;
		try {
			dos = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ParserFriends pf = new ParserFriends();
		
		this.info = pf.getUserByIp(socket.getInetAddress().getHostAddress());
		//本机ip地址的获取方式InetAddress.getLocalHost().getHostAddress()
		//监听到的对方的ip获取方式socket.getInetAddress().getHostAddress()
		if(null == info)
		{
			info = new String[]{"好友"};
		}
		self = pf.getUserByName("self")[1];
	}
	/**
	 * 服务器窗口
	 */
	public void serverFrame(String info) {
		

		if(null == this.info)
		{
			setTitle("好友");
		}
		else
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
		send.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// 获取到文本内容，发送到服务端
				String t = text.getText();
				send(t);
				// 每次点击发送后，清空文本框
				text.setText("");
				showText.append(self+"  "+new Date(System.currentTimeMillis()).toLocaleString()+"\n    "+t+"\n");
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
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosed(WindowEvent e) {
				
				super.windowClosed(e);
				try {
					in.close();
					dos.close();
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		setVisible(true);

		//设置发送的第一条消息
		showText.append(this.info[0]+"  "+new Date(System.currentTimeMillis()).toLocaleString()+"\n    "+info+"\n");
		while (flag) {
			try {
				receiver();
			} catch (Exception e1) {
			
				flag = false;
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 向客户端写数据
	 * 
	 * @param content
	 */
	public void send(String content) {
		try {
			// 将本身的十六进制字符串转换成十六进制字节数组,再发送出去
			dos.println(content);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// dos.println(content);

	}

	/**
	 * 接受数据并写到框架中
	 * 
	 * @return
	 */
	public void receiver() throws Exception {

		String value = "";
		if(null != in)
		{
			while (null != (value = in.readLine())) {
				if (!"".equals(value.trim())) {
					showText.append(info[0]+"  "+new Date(System.currentTimeMillis()).toLocaleString()+"\n    "+value+"\n");
				}
			}
		}

		return;
	}

}
