package com.wechat.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * 模拟发送请求-工具类
 *
 * @author 陈俊华
 * @date 2018年1月19日
 */
@Slf4j
public class HttpsUtil {

    /**
     * 发送get请求，返回响应流
     */
    public static InputStream get(String url, Map<String, Object> query, Map<String, String> headers,
        boolean resultStream) throws Exception {
        log.info("url         --> {}", url);
        log.info("args[]      --> {}", query == null ? null : query.toString());
        log.info("headers[]   --> {}", headers == null ? null : headers.toString());
        log.info("method      --> {}", "GET");
        log.info("resultStream  --> {}", resultStream);

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
        if (headers != null) {
            for (String item : headers.keySet()) {
                get.addRequestHeader(item, headers.get(item));
            }
        }

        System.setProperty("jsse.enableSNIExtension", "false");

        client.executeMethod(get);
        log.info("responseContentLength  --> {}, responseCharSet  --> {} ",
            get.getResponseContentLength(),
            get.getResponseCharSet());
        return get.getResponseBodyAsStream();
    }

    /**
     * 发送get请求，返回响应主体字符串
     *
     * @author cjh
     * @date 2019/5/7 15:41
     */
    public static String get(String url, Map<String, Object> query) throws Exception {
        log.info("url     --> {}", url);
        log.info("args[]  --> {}", query == null ? null : query.toString());
        log.info("method  --> {}", "GET");

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
        BufferedReader br = new BufferedReader(new InputStreamReader(get.getResponseBodyAsStream(), "utf-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        log.info("response  --> {}", sb);
        return sb.toString();
    }

    /**
     * 发送get请求，返回响应主体字符串
     *
     * @author cjh
     * @date 2019/5/7 15:41
     */
    public static String get(String url, Map<String, Object> query, Map<String, String> headers) throws Exception {
        log.info("url     --> {}", url);
        log.info("args[]  --> {}", query == null ? null : query.toString());
        log.info("headers  --> {}", headers == null ? null : headers.toString());
        log.info("method  --> {}", "GET");

        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        if (!query.isEmpty()) {
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

        //application/x-www-form-urlencoded;
        get.addRequestHeader("Content-Type", "charset=UTF-8");
        if (!headers.isEmpty()) {
            for (String item : headers.keySet()) {
                get.addRequestHeader(item, headers.get(item));
            }
        }

        System.setProperty("jsse.enableSNIExtension", "false");

        client.executeMethod(get);
        String responseBodyAsString = get.getResponseBodyAsString();
        String responseCharSet = get.getResponseCharSet();
        if (responseCharSet.contains("8859")) {
            log.info("responseCharSet  --> {} --> {}", responseCharSet, StandardCharsets.UTF_8);
            byte[] bytes = responseBodyAsString.getBytes(StandardCharsets.ISO_8859_1);
            responseBodyAsString = new String(bytes, StandardCharsets.UTF_8);
        }
        long responseContentLength = get.getResponseContentLength();
        if (responseContentLength > 1000) {
            log.info("responseContentLength  --> {}", responseContentLength);
        } else {
            log.info("response  --> {}", responseBodyAsString);
        }
        return responseBodyAsString;
    }

    /**
     * 发送get请求，返回响应头(headers)和响应主体(body)
     *
     * @author cjh
     * @date 2019/5/7 15:41
     */
    public static StreamAndHeaders getReturnHeadAndBody(String url, Map<String, Object> query) throws Exception {
        log.info("url     --> {}", url);
        log.info("args[]  --> {}", query == null ? null : query.toString());
        log.info("method  --> {}", "GET");

        StreamAndHeaders result = new StreamAndHeaders();
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

        result.setBody(get.getResponseBodyAsStream());
        Header[] responseHeaders = get.getResponseHeaders();
        result.setHeaders(JSONArray.fromObject(responseHeaders));

        log.info("response  --> {}", result.toString());
        return result;
    }

    /**
     * 发送post请求
     */
    public static String post(String url, Map<String, Object> map) throws Exception {
        log.info("url     --> {}", url);
        log.info("args[]  --> {}", map == null ? null : map.toString());
        log.info("method  --> {}", "POST");

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
        log.info("response  --> {}", sb);
        return sb.toString();
    }

    /**
     * 发送post请求
     */
    public static String post(String url, Map<String, Object> query, Map<String, Object> body,
        Map<String, String> headers) throws Exception {
        log.info("url     --> {}", url);
        log.info("query   --> {}", query == null ? null : query.toString());
        log.info("body  --> {}", body == null ? null : body.toString());
        log.info("headers  --> {}", headers == null ? null : headers.toString());
        log.info("method  --> {}", "POST");

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

        if (body != null) {
            RequestEntity requestEntity = new StringRequestEntity(JSONObject.fromObject(body).toString());
            post.setRequestEntity(requestEntity);
        }
        if (headers != null) {
            for (String item : headers.keySet()) {
                post.addRequestHeader(item, headers.get(item));
            }
        }

        client.executeMethod(post);

        BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), "utf-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        log.info("response  --> {}", sb);
        return sb.toString();
    }

    /**
     * 发送post请求
     */
    public static String post(String url, Map<String, Object> query, Map<String, Object> body) throws Exception {
        log.info("url     --> {}", url);
        log.info("query   --> {}", query == null ? null : query.toString());
        log.info("body  --> {}", body == null ? null : body.toString());
        log.info("method  --> {}", "POST");

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

        if (body != null) {
            RequestEntity requestEntity = new StringRequestEntity(JSONObject.fromObject(body).toString());
            post.setRequestEntity(requestEntity);
        }
        client.executeMethod(post);

        BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), "utf-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        log.info("response  --> {}", sb);
        return sb.toString();
    }

    /**
     * 下载图片
     *
     * @author cjh
     * @date 2019/5/7 15:41
     */
    public static String downFile(String url, String downPath) throws Exception {

        log.info("url       --> {}", url);
        log.info("downPath  --> {}", downPath);
        // 建立连接
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(url);
        client.executeMethod(get);
        for (Header item : get.getResponseHeaders()) {
            System.out.println(item);
        }
        // 获取响应头的文件名字段
        // String status = get.getResponseHeader("Content-Type").getValue().toString();
        // if (status.contains("application/json")) {
        // throw new MyException(get.getResponseBodyAsString());
        // }
        // String fileName =
        // get.getResponseHeader("Content-disposition").getValue().toString();
        // // 正则匹配文件名
        // Matcher mc = Pattern.compile("filename=\"([^\"]+)\"").matcher(fileName);
        // if (mc.find()) {
        // fileName = mc.group(1);
        // }
        // String filePathAndName = downPath + fileName;
        // log.info("filePath  --> {}", filePathAndName);
        InputStream in = get.getResponseBodyAsStream();
        String fileName = downPath + UUID.randomUUID() + ".jpeg";
        File file = new File(fileName);
        OutputStream out = new FileOutputStream(file);
        int line;
        while ((line = in.read()) != -1) {
            out.write(line);
        }
        out.close();
        in.close();

        log.info("file --> {}", fileName);
        return file.getName();
    }

    /**
     * 上传图片
     */
    public static String upload(String url, Map<String, String> headers, Map<String, Object> query,
        Map<String, Object> body, File file) throws Exception {
        log.info("url     --> {}", url);
        log.info("query   --> {}", query == null ? null : query.toString());
        log.info("body  --> {}", body == null ? null : body.toString());
        log.info("headers  --> {}", headers == null ? null : headers.toString());
        log.info("method  --> {}", "POST");
        log.info("file  --> {}", file);

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod(url);

        if (headers != null) {
            for (String item : headers.keySet()) {
                post.addRequestHeader(item, headers.get(item));
            }
        }

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

        Part[] parts = null;

        if (body != null) {
            List<Part> list = new ArrayList<>();
            list.add(new FilePart("filename", file));
            for (String item : body.keySet()) {
                list.add(new StringPart(item, body.get(item).toString()));
            }
            parts = new Part[list.size()];
            parts = list.toArray(parts);
        }

        post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));

        client.executeMethod(post);

        BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), "utf-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        log.info("response  --> {}", sb);
        return sb.toString();
    }

}
