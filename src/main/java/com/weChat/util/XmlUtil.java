package com.weChat.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 消息与XML格式转换工具类
 * 
 * @author 13413527259
 *
 */
public class XmlUtil {

	/**
	 * 解析XML(请求)
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseXml(InputStream is) throws Exception {
		// 将解析结果存储在HashMap中
		Map<String, Object> map = new HashMap<String, Object>();
		SAXReader reader = new SAXReader();
		Document doc = reader.read(is);
		Element root = doc.getRootElement();
		List<Element> nodes = root.elements();
		for (Element item : nodes) {
			map.put(item.getName(), item.getText());
		}
		return map;
	}

}
