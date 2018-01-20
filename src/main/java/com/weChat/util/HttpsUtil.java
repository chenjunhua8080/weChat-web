package com.weChat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

/**
 * 模拟发送请求-工具类
 * 
 * @author 陈俊华
 * @date 2018年1月19日
 */
public class HttpsUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpsUtil.class);

	public static InputStream get(String url, Map<String, Object> query, boolean resultStream) throws Exception {
		logger.info("url={}", url);
		logger.info("args[]={}", query == null ? null : query.toString());
		logger.info("method={}", "GET");
		logger.info("resultStream={}", resultStream);

		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		if (query != null) {
			StringBuilder sb = new StringBuilder();
			List<String> list = new ArrayList<>(query.keySet());
			for (int i = 0; i < list.size(); i++) {
				String key = list.get(i);
				sb.append(key + "=" + query.get(key) + "&");
			}
			String params = sb.toString();
			params = params.substring(0, params.lastIndexOf('&'));
			get.setQueryString(params);
		}

		System.setProperty("jsse.enableSNIExtension", "false");

		client.executeMethod(get);
		System.err.println(get.getQueryString());
		return get.getResponseBodyAsStream();
	}

	public static String get(String url, Map<String, Object> query) throws Exception {
		logger.info("url={}", url);
		logger.info("args[]={}", query == null ? null : query.toString());
		logger.info("method={}", "GET");

		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		if (query != null) {
			StringBuilder sb = new StringBuilder();
			List<String> list = new ArrayList<>(query.keySet());
			for (int i = 0; i < list.size(); i++) {
				String key = list.get(i);
				sb.append(key + "=" + query.get(key) + "&");
			}
			String params = sb.toString();
			params = params.substring(0, params.lastIndexOf('&'));
			get.setQueryString(params);
		}

		System.setProperty("jsse.enableSNIExtension", "false");

		client.executeMethod(get);
		System.err.println(get.getQueryString());
		BufferedReader br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), "utf-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		logger.info("response={}", sb);
		return sb.toString();
	}

	public static String post(String url, Map<String, Object> map) throws Exception {
		logger.info("url={}", url);
		logger.info("args[]={}", map == null ? null : map.toString());
		logger.info("method={}", "POST");

		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(url);

		if (map != null) {
			RequestEntity requestEntity = new StringRequestEntity(JSONObject.fromObject(map).toString());
			post.setRequestEntity(requestEntity);
		}
		client.executeMethod(post);

		BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), "utf-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		logger.info("response={}", sb);
		return sb.toString();
	}

	public static String post(String url, Map<String, Object> query, Map<String, Object> map) throws Exception {
		logger.info("url={}", url);
		logger.info("query={}", query == null ? null : query.toString());
		logger.info("args[]={}", map == null ? null : map.toString());
		logger.info("method={}", "POST");

		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(url);

		/**
		 * setQueryString自动编码导致ticket的‘@’改变，ret=1
		 */
		if (query != null) {
			NameValuePair[] params = new NameValuePair[query.size()];
			List<String> list = new ArrayList<>(query.keySet());
			for (int i = 0; i < list.size(); i++) {
				String key = list.get(i);
				NameValuePair nameValuePair = new NameValuePair(key, query.get(key).toString());
				params[i] = nameValuePair;
			}
			post.setQueryString(params);
		}

		if (map != null) {
			RequestEntity requestEntity = new StringRequestEntity(JSONObject.fromObject(map).toString());
			post.setRequestEntity(requestEntity);
		}
		client.executeMethod(post);

		BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), "utf-8"));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		logger.info("response={}", sb);
		return sb.toString();
	}

	public static String downFile(String url, Map<String, Object> map, String downPath) throws Exception {

		logger.info("url={}", url);
		logger.info("args[]={}", map.toString());
		logger.info("downPath={}", downPath);
		logger.info("method={}", "POST");
		// 建立连接
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		client.executeMethod(get);
		for (Header item : get.getResponseHeaders()) {
			System.err.println(item);
		}
		// 获取响应头的文件名字段
		// String status = get.getResponseHeader("Content-Type").getValue().toString();
		// if (status.contains("application/json")) {
		// throw new Exception(get.getResponseBodyAsString());
		// }
		// String fileName =
		// get.getResponseHeader("Content-disposition").getValue().toString();
		// // 正则匹配文件名
		// Matcher mc = Pattern.compile("filename=\"([^\"]+)\"").matcher(fileName);
		// if (mc.find()) {
		// fileName = mc.group(1);
		// }
		// String filePathAndName = downPath + fileName;
		// logger.info("filePath={}", filePathAndName);
		InputStream in = get.getResponseBodyAsStream();
		File file = new File(downPath + UUID.randomUUID() + ".jpeg");
		OutputStream out = new FileOutputStream(file);
		int line = -1;
		while ((line = in.read()) != -1) {
			out.write(line);
		}
		out.close();
		in.close();

		return file.getName();
	}

}
