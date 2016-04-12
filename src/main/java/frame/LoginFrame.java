package frame;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.gson.Gson;

import util.NIOTools;
import util.ParserFriends;

/**
 * 用户登录程序，做一下两个步骤 发送登录数据，并接受服务器的返回数据，更新信息
 * 
 * @author CasparWang
 * 
 */
public class LoginFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4844062270711004161L;
	
	public static void main(String[] args) {
		LoginFrame frame = new LoginFrame();
		frame.loginFrame();
	}

	/**
	 * 登录窗口
	 */
	@SuppressWarnings("serial")
	public void loginFrame() {
		setTitle("登录窗口");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 500);
		// 窗体居中
		setLocationRelativeTo(null);
		Container content = getContentPane();
		content.setBackground(Color.white);
		content.setLayout(null);

		JLabel labelName = new JLabel("用户名:");
		final JTextField name = new JTextField(10);
		name.setBorder(BorderFactory.createLoweredBevelBorder());
		JLabel labelPwd = new JLabel("密    码:");
		final JTextField password = new JTextField(10);
		password.setBorder(BorderFactory.createLoweredBevelBorder());
		JButton login = new JButton("登录");
		login.addActionListener(new ActionListener() {

			/**
			 * 和服务端建立连接
			 */
			public void actionPerformed(ActionEvent e) {
				connectServer(name.getText()+":"+password.getText());
			}

		});
		JPanel background = new JPanel(){
			protected void paintComponent(Graphics g) {
				ImageIcon icon = new ImageIcon(this.getClass().getResource("/image/pair.jpg"));
				Image img = icon.getImage();
				g.drawImage(img, 0, 0, LoginFrame.this.getWidth(), LoginFrame.this.getHeight(),
						icon.getImageObserver());

			}
		};
		background.setBounds(0, 0, this.getWidth(),this.getHeight());
		background.setLayout(null);
		
		JPanel panel = new JPanel(){
			protected void paintComponent(Graphics g) {
				ImageIcon icon = new ImageIcon(this.getClass().getResource("/image/login.jpg"));
				Image img = icon.getImage();
				g.drawImage(img,LoginFrame.this.getWidth(),100,
						icon.getImageObserver());

			}
		};
		panel.setBounds(50,this.getHeight()-250, this.getWidth()-100,100);
		panel.setOpaque(true);
		panel.add(labelName);
		panel.add(name);
		panel.add(labelPwd);
		panel.add(password);
		panel.add(login);
  
		background.add(panel);
		
		add("Center",background);

		setVisible(true);
	}

	/**
	 * 与服务端建立连接
	 */
	public synchronized void connectServer(String user) {
		try {
			ParserFriends pf = new ParserFriends();
			String[] info = pf.getUserByName("server");
			SocketChannel  channel = SocketChannel.open(
					new InetSocketAddress(info[1], 5200));
			while(!channel.finishConnect())
			{
				//doSomething
			}
			channel.write(NIOTools.stringToByteBuffer(user));
			// 获得返回数据进行设置
			Gson gson = new Gson();
			String users = NIOTools.byteBufferToString(channel).trim();
			//gson转换的map为<string,List>
			@SuppressWarnings("unchecked")
			Map<String,List<String>> map = gson.fromJson(users, LinkedHashMap.class);
			//将self的第一个属性设置成登录人的名字
			List<String> temp= map.get("self");
			temp.set(0,user.split(":")[0]);
			map.put("self",temp);
			//根据服务端传回的数据，更新好友信息
			pf.updateFriends(map);
			
			setVisible(false);
        	// 进入登录成功界面
        	SuccessFrame success = new SuccessFrame();
            success.successFrame();
            
		} catch (Exception e) {
			System.out.println("服务器端不存在");
		}	
	}
}
