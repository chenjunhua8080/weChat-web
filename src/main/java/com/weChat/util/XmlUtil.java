package com.weChat.util;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 消息与XML格式转换工具类
 *
 * @author cjh
 * @date 2019/5/7 11:02
 */
@Slf4j
public class XmlUtil {

    /**
     * 解析XML
     *
     * @param inputStream xml流的形式
     */
    public static Map<String, Object> parseXml(InputStream inputStream) {
        // 将解析结果存储在HashMap中
        Map<String, Object> map = new HashMap<>();
        SAXReader reader = new SAXReader();
        Document doc = null;
        try {
            doc = reader.read(inputStream);
        } catch (DocumentException e) {
            log.error("解析XML错误");
        }
        Element root = doc.getRootElement();
        List<Element> nodes = root.elements();
        for (Element item : nodes) {
            map.put(item.getName(), item.getText());
        }
        return map;
    }

    /**
     * 解析XML(请求)
     *
     * @param xmlStr xml字符串
     * @param charset 默认utf-8
     */
    public static Map<String, Object> parseXml(String xmlStr, String charset) throws Exception {
        if (charset == null || charset.equals("")) {
            charset = "utf-8";
        }
        // 将解析结果存储在HashMap中
        Map<String, Object> map = new HashMap<>();
        SAXReader reader = new SAXReader();
        byte[] bytes = xmlStr.getBytes(charset);
        Document doc = reader.read(new ByteInputStream(bytes, bytes.length));
        Element root = doc.getRootElement();
        List<Element> nodes = root.elements();
        for (Element item : nodes) {
            map.put(item.getName(), item.getText());
        }
        return map;
    }

}
