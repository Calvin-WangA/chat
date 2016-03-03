package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 该类通过Dom4j实现xml文档的创建与解析
 * 
 * @author CasparWang
 * 
 */
@SuppressWarnings("unchecked")
public class ParserFriends
{

	private Map<String,Map<String,String[]>> map;

	private Map<String,String[]> user;
	
	/**
	 * 产生相应的xml文件
	 */
	public void createXml(String fileName)
	{
		//获得当前的document
		Document document = DocumentHelper.createDocument();
		//产生一个根元素
		Element employees = document.addElement("employees");
		//通过循环迭代等操作实现相应元素的添加
		Element employee = employees.addElement("employee");
		//为相应的元素添加属性及值
		employee.addAttribute("id","1");
		Element name = employee.addElement("name");
		//为元素设置相应的值
		name.setText("ddvip");
		Element sex = employee.addElement("sex");
		sex.setText("m");
		Element age = employee.addElement("age");
		age.setText("29");
		try
		{
			File file = new File(fileName);
			if(!file.exists())
			{
				file.createNewFile();
			}
			//获得文件的writer
			Writer fileWriter = new FileWriter(file);
			XMLWriter xmlWriter = new XMLWriter(fileWriter);
			//通过该方法将上面的内容写到fileName中
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (IOException e)
		{

			System.out.println(e.getMessage());
		}

	}
	
	public String[] getUserByIp(String ip)
	{
		//String path = MyPath.getProjectPath()+"/friends.xml";
        String path = "friends.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		String[] user = null;
		Document document = null;
		try {
			// 获得解析文件的上下文
			document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			// 通过迭代获得相应的元素及值
			user = getUser(employees,ip);

		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
		return user;
	}
	
	/**
	 * 通过ip迭代获得用户信息
	 * @param element
	 * @param ip
	 * @return
	 */
	public String[] getUser(Element element,String ip)
	{
		List<Element> elements = element.elements();
		String[] user = null;
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {
			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				user = getIp(elements.get(i),ip);
				if(null != user)
				{
					break;
				}
			}
		}
		// 否则直接获取该元素的属性或者值
		else {
			List<Attribute> attributes = element.attributes();
			if(ip.equals(attributes.get(0).getValue().trim()))
			{
				user = new String[4];
				user[0] = element.getName();
				user[1] = ip;
				user[2] = attributes.get(1).getValue();
				user[3] = attributes.get(2).getValue();
				return user;
			}
		}
		return user;
	}
	/**
	 * 根据用户名获得ip地址
	 * @param name
	 * @return
	 */
	public String[] getUserByName(String name)
	{
		//String path = MyPath.getProjectPath()+"/friends.xml";
        String path = "friends.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		String[] user = null;
		Document document = null;
		try {
			// 获得解析文件的上下文
			document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			// 通过迭代获得相应的元素及值
			user = getIp(employees,name);

		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
		return user;
	}
	
	/**
	 * 迭代比较用户名，获得用户
	 * @param element 当前元素
	 * @param name 用户名
	 * @return 返回用户信息
	 */
	public String[] getIp(Element element,String name)
	{
		List<Element> elements = element.elements();
		String[] user = null;
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {
			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				user = getIp(elements.get(i),name);
				if(null != user)
				{
					break;
				}
			}
		}
		// 否则直接获取该元素的属性或者值
		else {
			if(name.equals(element.getName()))
			{
				user = new String[4];
				List<Attribute> attributes = element.attributes();
				user[0] = element.getName();
				user[1] = attributes.get(0).getValue();
				user[2] = attributes.get(1).getValue();
				user[3] = attributes.get(2).getValue();
				return user;
			}
		}
		return user;
	}
	
	/**
	 * 当在线的时候，如果有人上下线，则调用该方法更新本地配置文件
	 * @param info
	 */
	public void updateFriend(String[] info)
	{
		//String path = MyPath.getProjectPath()+"/friends.xml";
	    String path = "friends.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			// 获得解析文件的上下文
			document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			// 通过迭代获得相应的元素及值
			updateSingle(employees,info);

			// 格式化输出文档
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			// 输出全部原始数据，并用它生成新的我们需要的XML文件
			XMLWriter writer2 = new XMLWriter(new FileWriter(new File(path)),
					format);
			writer2.write(document); // 输出到文件
			writer2.close();

		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 更新某个好友信息
	 * @param element
	 * @param map
	 */
	public void updateSingle(Element element,String[] info)
	{
		List<Element> elements = element.elements();
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {
			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				updateSingle(elements.get(i),info);
			}
		}
		// 否则直接获取该元素的属性或者值
		else {
			if(info[0].trim().equals(element.getName()))
			{
				List<Attribute> attributes = element.attributes();
				// 如果有属性，则打印所有属性值
				if (attributes.size() > 0) {
					attributes.get(0).setValue(info[1]);
					attributes.get(1).setValue(info[2]);
					attributes.get(2).setValue(info[3]);
				}
			}
		}
	}
	
	/**
	 * 更新friends.xml的文件内容
	 * @param map
	 */
	public void updateFriends(Map<String,List<String>> infos)
	{
		//在jar包类的直接使用包名到文件名即可使用
		//String path = "xml/friends.xml";
		//如果是运行jar包外则使用MyPath获得当前jar路径
		//String path = MyPath.getProjectPath()+"/friends.xml";
		String path = "friends.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			// 获得解析文件的上下文
			document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			// 通过迭代获得相应的元素及值
			update(employees,infos);

			// 格式化输出文档
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			// 输出全部原始数据，并用它生成新的我们需要的XML文件
			XMLWriter writer2 = new XMLWriter(new FileWriter(new File(path)),
					format);
			writer2.write(document); // 输出到文件
			writer2.close();

		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println(e.getMessage());
		}
		return;
	}

	/**
	 * 实现更新当前所有好友信息
	 * @param element
	 * @param map
	 */
	public void update(Element element,Map<String,List<String>> infos)
	{
		List<Element> elements = element.elements();
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {
			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				update(elements.get(i),infos);
			}
		}
		// 否则直接获取该元素的属性或者值
		else {
			if(null != infos.get(element.getName()))
			{
				List<String> info = infos.get(element.getName());
				List<Attribute> attributes = element.attributes();
				// 如果有属性，则打印所有属性值
				if (attributes.size() > 0) {
					attributes.get(0).setValue(info.get(0));
					attributes.get(1).setValue(info.get(1));
					attributes.get(2).setValue(info.get(2));
				}
			}
		}
	}
	
	/**
	 * 解析相应的xml文件
	 */
	public Map<String,Map<String,String[]>> parserXml(String fileName)
	{
		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try
		{
			//获得解析文件的上下文
			Document document = saxReader.read(inputXml);
			//得到相应的根元素
			Element employees = document.getRootElement();
			map = new LinkedHashMap<String,Map<String,String[]>>(); 
			//通过迭代获得相应的元素及值
			parser(employees);
			
		} catch (DocumentException e)
		{
			System.out.println(e.getMessage());
		}
		
		return map;
	}
	
	/**
	 * 迭代实现元素的遍历
	 * @param element
	 */
	public void parser(Element element)
	{
		List<Element> elements = element.elements();
		List<Attribute> attributes = element.attributes();
		String[] attrs = null;
		//如果有属性，则打印所有属性值
		if(attributes.size() > 0)
		{
			attrs = new String[attributes.size()];
			for(int i = 0; i < attributes.size(); i++)
			{
				Attribute attribute = attributes.get(i);
				attrs[i] = attribute.getValue();
			}
			//将好友及好友信息放到一个map中
			user.put(element.getName(),attrs);
		}
		//如果条件满足则有子元素，继续迭代
		if(elements.size() > 0)
		{
			if(elements.get(0).elements().size() < 1)
			{
				//针对每一个组，产生一个用户map
				user = new LinkedHashMap<String,String[]>();
				map.put(element.getName(),user);
				//对每个元素进行相应的迭代
				for(int i = 0; i < elements.size(); i++)
				{
					parser(elements.get(i));
				}
			}
			else
			{
				//对每个元素进行相应的迭代
				for(int i = 0; i < elements.size(); i++)
				{
					parser(elements.get(i));
				}
			}
					
		}
		//否则直接获取该元素的属性或者值
		else
		{
			
			//System.out.println(element.getName()+":"+element.getText().trim());
		}
	}

}
