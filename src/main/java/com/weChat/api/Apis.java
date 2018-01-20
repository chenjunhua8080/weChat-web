package com.weChat.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import com.weChat.util.HttpsUtil;
import com.weChat.util.XmlUtil;

import net.sf.json.JSONObject;

/**
 * web微信用到的API
 * 
 * @author 陈俊华
 * @date 2018年1月19日
 */
public final class Apis {

	public final static String jsLogin = "https://login.wx2.qq.com/jslogin";

	public final static String qrcode = "https://login.weixin.qq.com/qrcode/UUID";

	/**
	 * 3. 等待登录（参考方法 waitForLogin）这里是微信确认登录 method GET params tip : 1:未扫描 0:已扫描 uuid
	 * : 获取到的uuid _ : 时间戳 返回数据(String):window.code=xxx;408,201,200
	 */
	public final static String waitForLogin = "https://login.wx2.qq.com/cgi-bin/mmwebwx-bin/login";

	/**
	 * 4. 登录获取Cookie（参考方法 login） method GET params ticket:xxx uuid:xxx lang:xxx
	 * scan:扫码成功后返回的时间戳（s） fun:new version:v2
	 * 返回数据(XML)：解析可以得到：skey、sid、uin、pass_ticket的值。
	 */
	public final static String loginPage = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage";

	/**
	 * 5. 微信初始化（参考方法 wxInit） method POST params BaseRequest: { DeviceID:”xxx”, Sid:
	 * “xxx”, Skey: “xxx”, Uin: “xxx”, } PS:DeviceID值的由来：e+15位随机数，JS中的实现如下：
	 * https://wx.qq.com 报错1100
	 */
	public final static String init = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxinit";

	/**
	 * 6. 开启微信状态通知（参考方法 wxStatusNotify） method POST data JSON params BaseRequest: {
	 * ClientMsgId:时间戳（ms） Code:3 FromUserName:”自己的ID” ToUserName:”自己的ID” }
	 */
	public final static String statusnotify = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxstatusnotify";

	/**
	 * 7. 获取联系人列表（参考方法 getContact） method post params lang=zh_CN pass_ticket=xxx
	 * r=xxx seq=0 skey=xxx
	 */
	public final static String getcontact = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact";

	/**
	 * method POST params BaseRequest: { DeviceID:”xxx” Sid:”xxx” Skey:”xxx” Uin:xxx
	 * } Count:4 List: [ 0:{UserName: “xxx”, EncryChatRoomId: “”} 1:{UserName:
	 * “xxx”, ChatRoomId: “”} … ]
	 */
	public final static String batchgetcontact = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxbatchgetcontact";

	/**
	 * 10. 发送消息（参考方法 webwxsendmsg） method POST params BaseRequest: { DeviceID:”xxx”
	 * Sid:”xxx” Skey:”xxx” Uin:xxx } Msg: { ClientMsgId:”14672041846800613”
	 * Content:”hello, myself.” FromUserName:”xxx” LocalID:”14672041846800613”
	 * ToUserName:”filehelper” Type:1 } Scene:0
	 */
	public final static String sendmsg = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg?pass_ticket=PASS_TICKET";

	/**
	 * 1. 获取UUID（参考方法 getUUID） method GET Params appid：wx782c26e4c19acffb，应用ID（固定值）
	 * redirect_uri:https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage fun :new
	 * lang : en_US 或 zh_CN，浏览器的语言 _ : 1466394395577，时间戳（ms，js中的实现为new Date）
	 * 返回数据(String): window.QRLogin.code = 200; window.QRLogin.uuid = "xxx"
	 * 
	 * @return UUID
	 * @throws Exception
	 */
	public static String jsLogin() throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("appid", "wx782c26e4c19acffb");
		map.put("fun", "new");
		map.put("lang", "zh_CN");
		// map.put("redirect_uri",
		// "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage");
		map.put("_", System.currentTimeMillis());
		JSONObject resp = JSONObject.fromObject("{" + HttpsUtil.get(jsLogin, map) + "}");
		return resp.getString("window.QRLogin.uuid");
	}

	/**
	 * 2. 显示二维码（参考方法 showQrCode） method POST params t : webwx <br/>
	 * _ : 时间戳 返回的数据： 一张二维码图片
	 * 
	 * @return 图片名称
	 * @throws Exception
	 */
	public static String getQrcode(String uuid) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("t", "webwx");
		map.put("_", System.currentTimeMillis());
		String resp = HttpsUtil.downFile(qrcode.replace("UUID", uuid), map, "c://");
		return resp;
	}

	/**
	 * 3. 等待登录，获取扫码结果
	 * 
	 * @param tip
	 * @param uuid
	 * @return window.redirect_uri/window.userAvatar
	 * @throws Exception
	 */
	public static Map<String, Object> waitForLogin(int tip, String uuid) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("loginicon", true);
		map.put("uuid", uuid);
		map.put("tip", tip);
		map.put("_", System.currentTimeMillis());
		JSONObject resp = JSONObject.fromObject("{" + HttpsUtil.get(waitForLogin, map) + "}");
		Map<String, Object> result = new HashMap<>();
		result.put("code", resp.getInt("window.code"));
		switch (resp.getInt("window.code")) {
		case 200:
			System.out.println("登录成功" + resp.toString());
			String redirect = resp.getString("window.redirect_uri");
			redirect+="&fun=new";
			result.put("uri",redirect);
			break;
		case 201:
			System.out.println("扫描成功，请确认登录" + resp.toString());
			break;
		case 408:
			System.out.println("扫描超时" + resp.toString());
			break;
		default:
			System.out.println("发现错误：" + resp.toString());
			break;
		}
		return result;
	}

	public static Map<String, Object> loginPage(Map<String, Object> map) throws Exception {
		if (new Integer(map.get("code").toString())==200) {
			return XmlUtil.parseXml(HttpsUtil.get(map.get("uri").toString(), null, true));
		}
		return map;
	}

	/**
	 * 
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static JSONObject init(Map<String, Object> args) throws Exception {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 15; i++) {
			sb.append(random.nextInt(10));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("DeviceID", "e" + sb.toString());
		map.put("Sid", args.get("wxsid"));
		map.put("Skey", args.get("skey"));
		map.put("Uin", args.get("wxuin"));
		Map<String, Object> base = new HashMap<>();
		base.put("BaseRequest", map);
		Map<String, Object> query = new HashMap<>();
		query.put("pass_ticket", args.get("pass_ticket"));
		JSONObject resp = JSONObject.fromObject(HttpsUtil.post(init, query, base));
		return resp;
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getcontact(Map<String, Object> args) throws Exception {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 15; i++) {
			sb.append(random.nextInt(10));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("DeviceID", "e" + sb.toString());
		map.put("Sid", args.get("wxsid"));
		map.put("Skey", args.get("skey"));
		map.put("Uin", args.get("wxuin"));
		Map<String, Object> base = new HashMap<>();
		base.put("BaseRequest", map);
		Map<String, Object> query = new HashMap<>();
		query.put("pass_ticket", args.get("pass_ticket"));
		JSONObject resp = JSONObject.fromObject(HttpsUtil.post(getcontact, query, base));
		return resp;
	}
	
	public static JSONObject batchgetcontact(Map<String, Object> args) throws Exception {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 15; i++) {
			sb.append(random.nextInt(10));
		}
		Map<String, Object> map = new HashMap<>();
		map.put("DeviceID", "e" + sb.toString());
		map.put("Sid", args.get("wxsid"));
		map.put("Skey", args.get("skey"));
		map.put("Uin", args.get("wxuin"));
		Map<String, Object> base = new HashMap<>();
		base.put("BaseRequest", map);
		Map<String, Object> query = new HashMap<>();
		query.put("pass_ticket", args.get("pass_ticket"));
		JSONObject resp = JSONObject.fromObject(HttpsUtil.post(batchgetcontact, query, base));
		return resp;
	}

	public static void main(String[] args) throws Exception {
		String uuid = jsLogin();
		getQrcode(uuid);
		System.err.println(uuid);
		Scanner scanner = new Scanner(System.in);
		int tip;
		while ((tip = scanner.nextInt()) != -1) {
			Map<String, Object> map;
			map = loginPage(waitForLogin(tip, uuid));
			System.err.println(map);
			if (map.get("pass_ticket") != null) {
				System.out.println(init(map));
				System.out.println(getcontact(map));
				System.out.println(batchgetcontact(map));
			}
		}

	}

}
