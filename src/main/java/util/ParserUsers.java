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
public class ParserUsers {

	private Map<String, String[]> map;

	/**
	 * 产生相应的xml文件
	 */
	public void createXml(String fileName) {
		// 获得当前的document
		Document document = DocumentHelper.createDocument();
		// 产生一个根元素
		Element employees = document.addElement("employees");
		// 通过循环迭代等操作实现相应元素的添加
		Element employee = employees.addElement("employee");
		// 为相应的元素添加属性及值
		employee.addAttribute("id", "1");
		Element name = employee.addElement("name");
		// 为元素设置相应的值
		name.setText("ddvip");
		Element sex = employee.addElement("sex");
		sex.setText("m");
		Element age = employee.addElement("age");
		age.setText("29");
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 获得文件的writer
			Writer fileWriter = new FileWriter(file);
			XMLWriter xmlWriter = new XMLWriter(fileWriter);
			// 通过该方法将上面的内容写到fileName中
			xmlWriter.write(document);
			xmlWriter.close();
		} catch (IOException e) {

			System.out.println(e.getMessage());
		}

	}

	/**
	 * 修改登录的用户地址
	 * 
	 * @param user
	 *            一个包含更新信息的集合
	 */
	public void modifyUserInfo(String[] user) {
		//String path = this.getClass().getResource("/").getFile()+"users.xml";
		//path = path.substring(0, path.length() - 4)+"users.xml";
		//String path = "users.xml";
		String path = System.getProperty("user.dir")+"/users.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			// 获得解析文件的上下文
			document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			// 通过迭代获得相应的元素及值
			modify(employees, user);

			// 格式化输出文档
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding("UTF-8");
			// 输出全部原始数据，并用它生成新的我们需要的XML文件
			XMLWriter writer2 = new XMLWriter(new FileWriter(new File(path)),
					format);
			writer2.write(document); // 输出到文件
			writer2.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	/**
	 * 迭代实现元素的遍历 更新用户地址
	 * 
	 * @param element
	 */
	@SuppressWarnings("unchecked")
	public void modify(Element element, String[] user) {
		List<Element> elements = element.elements();
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {
			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				modify(elements.get(i), user);
			}
		}
		// 否则直接获取该元素的属性或者值
		else {
			if (user[0].equals(element.getName())) {
				List<Attribute> attributes = element.attributes();
				// 如果有属性，则打印所有属性值
				if (attributes.size() > 0) {
					attributes.get(0).setValue(user[1]);
					attributes.get(1).setValue(user[2]);
					attributes.get(2).setValue(user[3]);
				}
			}

		}
	}

	/**
	 * 解析相应的xml文件
	 */
	public Map<String, String[]> parserXml() {
		//String path = this.getClass().getResource("/").getFile()+"users.xml";
		//path = path.substring(0, path.length() - 4)+"users.xml";
		//String path = "users.xml";
		String path = System.getProperty("user.dir")+"/users.xml";
		File inputXml = new File(path);
		SAXReader saxReader = new SAXReader();
		try {
			// 获得解析文件的上下文
			Document document = saxReader.read(inputXml);
			// 得到相应的根元素
			Element employees = document.getRootElement();
			map = new LinkedHashMap<String, String[]>();
			// 通过迭代获得相应的元素及值
			parser(employees);

		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		
		return map;
	}

	/**
	 * 迭代实现元素的遍历
	 * 
	 * @param element
	 */
	@SuppressWarnings("unchecked")
	public void parser(Element element) {
		List<Element> elements = element.elements();
		List<Attribute> attributes = element.attributes();
		String[] attrs = null;
		// 如果有属性，则打印所有属性值
		if (attributes.size() > 0) {
			attrs = new String[attributes.size()];
			for (int i = 0; i < attributes.size(); i++) {
				Attribute attribute = attributes.get(i);
				attrs[i] = attribute.getValue();
			}
			// 将好友及好友信息放到一个map中
			map.put(element.getName(), attrs);
		}
		// 如果条件满足则有子元素，继续迭代
		if (elements.size() > 0) {

			// 对每个元素进行相应的迭代
			for (int i = 0; i < elements.size(); i++) {
				parser(elements.get(i));
			}

		}
		// 否则直接获取该元素的属性或者值
		else {

			// System.out.println(element.getName()+":"+element.getText().trim());
		}
	}

}
