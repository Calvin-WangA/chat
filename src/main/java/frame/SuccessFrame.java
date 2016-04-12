package frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.NIOTools;
import util.ParserFriends;

/**
 * 登录成功后启动的页面，有好友信息，可主动建立连接，可接受连接 监听程序在登录成功后启动
 * 
 * @author CasparWang
 * 
 */
public class SuccessFrame extends JFrame implements Job
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7236492397815600L;

	/**
	 * 动态的设置组件垂直的位置
	 */
	private int height = 5;

	// 全局的位置变量，用于表示鼠标在窗口上的位置
	static Point origin = new Point();

	Map<String, Map<String, String[]>> map;

	private static Logger log = LoggerFactory.getLogger(SuccessFrame.class);

	private File file = new File("friends.xml");

	static Map<String, ChatFrame> frames;

	private static Long firstTime = 0L;

	/**
	 * 登录窗口
	 */
	public void successFrame()
	{
		frames = new HashMap<String, ChatFrame>();
		// 读取好友信息并将其显示
		ParserFriends parser = new ParserFriends();
		// 该map的参数为<组名，<好友名，好友信息>>
		// String path = MyPath.getProjectPath()+"/friends.xml";
		String path = "friends.xml";
		this.map = parser.parserXml(path);

		setTitle("self");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(300, 500);
		// 该设置去掉了窗口的所有的边框，相应的功能也需要重新了
		setUndecorated(true);
		// 窗体居中
		setLocationRelativeTo(null);
		// 头
		JPanel title = new MyPanel("/image/back.jpg");
		title.paintComponents(getGraphics());

		title.setLayout(null);
		title.setBorder(BorderFactory.createEtchedBorder());
		title.setBounds(0, 0, this.getWidth(), 100);
		getMouseClickPosition(title);
		// 添加组件实现关闭窗口功能
		JLabel shutdown = new JLabel("关闭");
		shutdown.setBounds(this.getWidth() - 30, 0, 30, 30);
		addShutDownListener(shutdown);
		title.add(shutdown);
		// 增加该监听器，拖动窗口
		title.addMouseMotionListener(new DragMoveAdapter());

		// 利用JPanel添加背景图片
		final JPanel back = new MyPanel("/image/pair.jpg");
		back.paintComponents(getGraphics());
		back.setLayout(null);
		back.setBounds(0, 0, this.getWidth(), this.getHeight());

		JPanel panel = new JPanel();
		panel.setLayout(null);
		// 设置为凹边型
		panel.setBorder(BorderFactory.createLoweredBevelBorder());
		panel.setBounds(20, 120, 200, 300);
		// 遍历好友，显示到登录页面
		setUserList(panel);

		back.add("North", title);
		back.add("Center", panel);

		add(back);
		setVisible(true);
		ListenerFileChange(panel);
	}

	/**
	 * 继承JPanel重写设置图片的方法
	 * 
	 * @author CasparWang
	 *
	 */
	private class MyPanel extends JPanel
	{

		private static final long serialVersionUID = 1734537877465680845L;

		String imgPath = "";

		MyPanel(String imgPath)
		{
			super();
			this.imgPath = imgPath;
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			ImageIcon icon = new ImageIcon(this.getClass().getResource(imgPath));
			Image img = icon.getImage();
			g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), icon.getImageObserver());

		}
	}

	private void getMouseClickPosition(JPanel title)
	{

		// 应该增加一个监听配置文件，当有改动时重新加载文件内容，还有更好的方法就是，每次要聊天时才去动态读取个人的地址
		// 增加该监听器，获得初始位置
		title.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				// 当鼠标按下的时候获得窗口当前的位置
				origin.x = e.getX();
				origin.y = e.getY();
			}
		});
	}

	public void addShutDownListener(JLabel shutdown)
	{
		shutdown.addMouseListener(new MouseAdapter()
		{
			/**
			 * 关闭该窗口
			 */
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// 这个是隐藏
				SuccessFrame.this.setVisible(false);
				// 点击关闭窗口时，需要通知服务器该用户已经退出登录，服务器通知其好友
				notifyExit();
				// 通知监听程序，然监听程序关闭

				// 这个是关闭当前程序
				System.exit(0);
			}
		});
	}

	/**
	 * 设置用户好友列表
	 * 
	 * @param panel
	 */
	public void setUserList(JPanel panel)
	{
		for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
		{

			final JLabel name = new JLabel(iter.next());
			name.setBounds(5, height, panel.getWidth() - 10, 30);
			name.setBorder((BorderFactory.createRaisedBevelBorder()));
			name.setOpaque(true);
			name.setBackground(Color.white);

			name.addMouseListener(new UpdateUIListener(panel, name));
			panel.add(name);
			height += 30;
		}
	}

	/**
	 * 通知服务端，该用户马上退出了
	 * 
	 * @param user
	 */
	public void notifyExit()
	{
		try
		{
			ParserFriends pf = new ParserFriends();
			// self是特例，第一个属性表示的是登录用户的id
			String name = pf.getUserByName("self")[1];
			String serverIp = pf.getUserByName("server")[1];
			SocketChannel channel = SocketChannel.open(new InetSocketAddress(serverIp, 5200));

			channel.write(NIOTools.stringToByteBuffer(name + ":exit"));
			channel.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 该方法为定时器提供的接口job的一个方法，通过在这里面实现定时要执行的逻辑
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		CronTrigger trigger = (CronTrigger) context.getTrigger();
		log.info("The next fire time is " + trigger.getNextFireTime());
		// String path = MyPath.getProjectPath()+"/friends.xml";

		Long lastTime = file.lastModified();
		// 表示被更新过，重现成功界面
		if ((lastTime - firstTime) > 0)
		{
			Scheduler sc = context.getScheduler();
			JPanel jp = null;
			try
			{
				jp = (JPanel) sc.getContext().get("panel");
			} catch (SchedulerException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 文件被更改时更新UI
			updateMyUI(jp);
			jp.updateUI();
		}

		firstTime = lastTime;
	}

	/**
	 * 当发现有有好友上下线时，更新界面
	 */
	public synchronized void updateMyUI(JPanel panel)
	{
		// 读取好友信息并将其显示
		ParserFriends parser = new ParserFriends();
		String path = "friends.xml";
		this.map = parser.parserXml(path);
		// 先获得当前UI的全部组件
		Component[] items = (Component[]) panel.getComponents();
		panel.removeAll();
		for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();)
		{
			String name = iter.next();
			Map<String, String[]> friends = map.get(name);

			for (int i = 0; i < items.length; i++)
			{

				JLabel temp = (JLabel) items[i];
				Point ptemp = temp.getLocation();
				String tName = temp.getText();
				// 增加组
				if (name.equals(tName))
				{
					panel.add(temp);
					continue;
				}
				// 增加组成员
				if (friends.containsKey(tName))
				{
					String[] friend = friends.get(tName);
					JLabel item = null;
					Point p = null;
					if ("1".equals(friend[2]))
					{
						temp.setBackground(Color.pink);
						// 和不在线的进行交换
						for (int j = i - 1; j >= 0; j--)
						{
							item = (JLabel) items[j];
							p = item.getLocation();
							if (p.x == 5 || ((item.getBackground() == Color.pink) && (i == j + 1)))
							{
								break;
							}
							// 组件位置进行替换
							if (((item.getBackground() == Color.pink) && (i != j + 1)))
							{
								item = (JLabel) items[j + 1];
								p = item.getLocation();
								panel.remove(item);
								item.setBounds(ptemp.x, ptemp.y, item.getWidth(), item.getHeight());
								items[i] = item;
								panel.add(item);
								temp.setBounds(p.x, p.y, temp.getWidth(), temp.getHeight());
								items[j + 1] = temp;
								break;
							}
						}
					} else
					{
						temp.setBackground(Color.white);
					}
					panel.add(temp);
				}

			}

		}

	}

	/**
	 * 每隔十秒钟扫描一次指定文件，当文件发生改变时，重新创建成功界面
	 */
	public void ListenerFileChange(JPanel panel)
	{
		try
		{
			log.info("-----------Initializing------------");
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler = sf.getScheduler();
			log.info("-----------Initializing Complete----------");

			log.info("-----------Starting Scheduler------------");
			JobDetail job = JobBuilder.newJob(this.getClass()).withIdentity("myJob", "group1").build();
			// 采用cronExpression来精确指定执行的时间,参考Developer Guide
			CronTrigger trigger = (CronTrigger) TriggerBuilder.newTrigger().withIdentity("trigger", "group1")
					.withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?")).build();
			scheduler.scheduleJob(job, trigger);
			scheduler.getContext().put("panel", panel);
			scheduler.start();

		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 该类实现了点击分组，实现好友信息的显示与隐藏
	 * 
	 * @author CasparWang
	 * 
	 */
	private class UpdateUIListener extends MouseAdapter
	{
		private JPanel panel;
		private JLabel name;
		private ParserFriends parser;

		public UpdateUIListener(JPanel panel, JLabel name)
		{
			this.panel = panel;
			this.name = name;
		}

		/**
		 * 点击该组名时，动态展开或者收缩组成员
		 */
		@Override
		public void mouseClicked(MouseEvent e)
		{

			// 重构UI
			rebuildUI();
			// 刷新当前panel的UI
			panel.updateUI();
		}

		/**
		 * 当我们点击组名时，增删相应的条目，因此需要重组UI，
		 */
		public void rebuildUI()
		{
			// 先获得当前UI的全部组件
			Component[] items = (Component[]) panel.getComponents();
			// 清空所有组件重新组装
			panel.removeAll();
			JLabel item = null;
			JLabel next = null;
			Point position = null;
			// 表示什么时候进行原有元素的重构
			boolean flag = false;
			// 重构所有所有元素
			for (int i = 0; i < items.length; i++)
			{
				item = (JLabel) items[i];
				position = item.getLocation();
				// 当相等执行操作后，后面的元素都会被重构
				if (flag)
				{
					item.setBounds(position.x, height, item.getWidth(), item.getHeight());
					panel.add(item);
					height += 30;
					continue;
				}
				// 还没有相等时原样增加组件
				panel.add(item);
				// 相等时对该组成员进行展示或者缩略
				if (name.getText().equals(item.getText()))
				{
					int j = 1;
					while (true)
					{
						height = position.y + 30;
						if (i + j < items.length)
						{
							next = (JLabel) items[i + j];
						}

						// 这里是展开组员，null条件为最后一个组名后没有元素的情况
						if (null == next || (next.getLocation().x == position.x && j == 1))
						{
							addItem();
							break;
						} else if (next.getLocation().x > position.x)
						{// 如果下一个组件的左距大于当前的组件，则清空大于的，将同级别的追加上，并且重新设置组件位置
							j++;
							if (i + j == items.length)// 证明已经没有元素了
							{
								break;
							}
							continue;
						} else
						{
							break;
						}
					}
					i = i + j - 1;
					flag = true;
				}

			}
		}

		/**
		 * 添加某个组的成员
		 */
		public void addItem()
		{
			// 读取好友信息并将其显示
			parser = new ParserFriends();
			String path = "friends.xml";
			map = parser.parserXml(path);
			// 得到某个分组的所以好友信息
			final Map<String, String[]> friends = sortedByFlag(map.get(name.getText()));
			String key = "";
			for (Iterator<String> iter = friends.keySet().iterator(); iter.hasNext();)
			{
				key = iter.next();
				String[] user = friends.get(key);
				final JLabel friend = new JLabel(key);
				friend.setBounds(40, height, panel.getWidth() - 45, 30);
				friend.setBorder((BorderFactory.createRaisedBevelBorder()));
				// 设置透明，这样才能显示
				friend.setOpaque(true);
				// 根据在线与否设置不同颜色
				if ("1".equals(user[2]))
				{
					friend.setBackground(Color.pink);
				} else
				{
					friend.setBackground(Color.white);
				}

				addFriendClientListener(friend);
				panel.add(friend);
				height += 30;
			}
		}

		public void addFriendClientListener(final JLabel friend)
		{
			friend.addMouseListener(new MouseAdapter()
			{
				Color color = friend.getBackground();

				/**
				 * 点击对应好友，与好友进行连接并聊天
				 */
				@SuppressWarnings("deprecation")
				@Override
				public void mouseClicked(MouseEvent e)
				{

					if (e.getClickCount() == 2)
					{
						String[] info = parser.getUserByName(friend.getText().trim());
						ChatFrame chat = frames.get(info[0]);
						// 传入地址建立连接,客户端在哪里监听的都是同一端口，唯一不同的就是地址,不在线不弹出框
						try
						{
							if (null == chat)
							{
								chat = new ChatFrame(info, 12345);

								frames.put(info[0], chat);
								chat.clientFrame();

							} else
							{
								chat.show();
								chat.connectReceive();
							}
						} catch (Exception e1)
						{
							e1.printStackTrace();
						}
					}
				};

				@Override
				public void mousePressed(MouseEvent e)
				{
					friend.setBackground(Color.red);
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					friend.setBackground(color);
				}

			});
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			name.setBackground(Color.red);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			name.setBackground(Color.white);
		}

		/**
		 * 展示界面以前将上下线的分开
		 * 
		 * @param users
		 */
		public Map<String, String[]> sortedByFlag(Map<String, String[]> users)
		{
			Map<String, String[]> friends = new LinkedHashMap<String, String[]>();
			String[] user = null;
			String key = null;
			// 将在线用户先放入map
			for (Iterator<String> iter = users.keySet().iterator(); iter.hasNext();)
			{
				key = iter.next();
				user = users.get(key);
				if ("1".equals(user[2]))
				{
					friends.put(key, user);
				}
			}
			// 再将不在线用户放到集合中
			for (Iterator<String> iter = users.keySet().iterator(); iter.hasNext();)
			{
				key = iter.next();
				user = users.get(key);
				if ("0".equals(user[2]))
				{
					friends.put(key, user);
				}
			}

			return friends;
		}
	}

	/**
	 * 实现一个拖动窗口的监听类
	 * 
	 * @author CasparWang
	 * 
	 */
	private class DragMoveAdapter extends MouseMotionAdapter
	{
		// 拖动（mouseDragged 指的不是鼠标在窗口中移动，而是用鼠标拖动）
		public void mouseDragged(MouseEvent e)
		{
			// 当鼠标拖动时获取窗口当前位置
			Point p = SuccessFrame.this.getLocation();
			// 设置窗口的位置
			// 窗口当前的位置 + 鼠标当前在窗口的位置 - 鼠标按下的时候在窗口的位置
			SuccessFrame.this.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
		}

	}
}
