package com.wechat.controller;

import com.wechat.po.response.SendMsgResponse;
import com.wechat.po.wechat.BaseResponsePO;
import com.wechat.po.wechat.ContactListPO;
import com.wechat.po.wechat.InitPO;
import com.wechat.po.wechat.LoginPagePO;
import com.wechat.po.wechat.SyncKeyPO;
import com.wechat.po.wechat.WebWxSyncPO;
import com.wechat.request.SendMsgRequest;
import com.wechat.service.RedisService;
import com.wechat.util.HttpsUtil;
import com.wechat.util.WeChatUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@RestController
public class UserController {

    @Autowired
    RedisService redisService;

    /**
     * 判断是否登录
     */
    @GetMapping("/isLogin")
    public LoginPagePO isLogin(@SessionAttribute(required = false) LoginPagePO loginPage) {
        if (loginPage != null) {
            return loginPage;
        }
        return null;
    }


    /**
     * 免扫码登录
     */
    @GetMapping("/notScanLogin")
    public String notScanLogin() throws Exception {
        String loginPagePOStr = redisService.get(WeChatUtil.LOGINPAGE, String.class);
        if (loginPagePOStr == null) {
            return null;
        }
        JSONObject jsonObject = JSONObject.fromObject(loginPagePOStr);
        LoginPagePO loginPagePO = (LoginPagePO) JSONObject.toBean(jsonObject, LoginPagePO.class);
        String uuid = WeChatUtil.notScanLogin(loginPagePO);
        if (uuid == null) {
            redisService.delete(WeChatUtil.USERAVATAR);
            redisService.delete(WeChatUtil.LOGINPAGE);
        }
        return uuid;
    }

    /**
     * 退出
     */
    @GetMapping("/logout")
    public String logout() throws Exception {
        String loginPagePOStr = redisService.get(WeChatUtil.LOGINPAGE, String.class);
        if (loginPagePOStr == null) {
            return null;
        }
        JSONObject jsonObject = JSONObject.fromObject(loginPagePOStr);
        LoginPagePO loginPagePO = (LoginPagePO) JSONObject.toBean(jsonObject, LoginPagePO.class);
        JSONObject logout = WeChatUtil.logout(loginPagePO);
        return logout.toString();
    }


    /**
     * 获取二维码链接
     */
    @GetMapping("/getQrCode")
    public Map<String, Object> getQrCode() throws Exception {
        //登陆过的不需要扫码
        String loginPagePO = redisService.get(WeChatUtil.LOGINPAGE, String.class);
        if (loginPagePO != null) {
            String userAvatar = redisService.get(WeChatUtil.USERAVATAR, String.class);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("userAvatar", userAvatar);
            return resultMap;
        } else {
            String uuid = WeChatUtil.jsLogin();
            //不生成图片了，直接返回链接
            //        String qrCode = WeChatUtil.getQrCode(uuid);
            String qrCode = WeChatUtil.qrCode.replace("UUID", uuid);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("uuid", uuid);
            resultMap.put("qrCode", qrCode);
            return resultMap;
        }
    }

    /**
     * 刷新登录状态，成功时初始化loginPage
     */
    @GetMapping("/getLoginStatus/{uuid}")
    public Map<String, Object> getLoginStatus(@PathVariable("uuid") String uuid,
        HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> loginStatus = WeChatUtil.waitForLogin(0, uuid);
        Object codeObj = loginStatus.get("code");
        if (codeObj != null) {
            int code = Integer.parseInt(codeObj.toString());
            if (code == 200) {
                LoginPagePO loginPage = WeChatUtil
                    .loginPage(loginStatus.get("ticket").toString(),
                        loginStatus.get("uuid").toString(),
                        loginStatus.get("scan").toString());
                //设置到session
                HttpSession session = request.getSession();
                session.setAttribute("loginPage", loginPage);
                //设置到cookie
                Cookie cookie7 = new Cookie("webwx_auth_ticket", loginPage.getWebwx_auth_ticket());
                Cookie cookie8 = new Cookie("webwx_data_ticket", loginPage.getWebwx_data_ticket());
                Cookie cookie9 = new Cookie("webwxuvid", loginPage.getWebwxuvid());
                Cookie cookie1 = new Cookie("sKey", loginPage.getSKey());
                Cookie cookie2 = new Cookie("wxSid", loginPage.getWxSid());
                Cookie cookie3 = new Cookie("wxUin", loginPage.getWxUin());
                Cookie cookie4 = new Cookie("passTicket", loginPage.getPassTicket());
                response.addCookie(cookie1);
                response.addCookie(cookie2);
                response.addCookie(cookie3);
                response.addCookie(cookie4);
                response.addCookie(cookie7);
                response.addCookie(cookie8);
                response.addCookie(cookie9);

                //登录后存到redis
                JSONObject jsonObject = JSONObject.fromObject(loginPage);
                redisService.set(WeChatUtil.LOGINPAGE, jsonObject);
            } else if (code == 201) {
                redisService.set(WeChatUtil.USERAVATAR, loginStatus.get("userAvatar"));
            }
        }
        return loginStatus;
    }

    /**
     * 初始化，顺便把chatSet的也安排到contactList里
     */
    @GetMapping("/init")
    public InitPO init(HttpServletRequest request) throws Exception {
        Object loginPageObject = request.getSession().getAttribute("loginPage");
        if (loginPageObject == null) {
            return null;
        }
        LoginPagePO loginPage = (LoginPagePO) loginPageObject;

        InitPO init = WeChatUtil
            .init(loginPage.getWxSid(), loginPage.getSKey(), loginPage.getWxUin(), loginPage.getPassTicket());

        //没有请求到正确数据，清除信息
        int ret = init.getBaseResponse().getRet();
        if (ret != 0) {
            request.getSession().removeAttribute("loginPage");
        }

        //处理batchContact
        init = WeChatUtil.batchGetContact(init, loginPage);

        //只返回初始化信息，否则数据量太大
        return init;
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/getContact")
    public ContactListPO getContact(HttpServletRequest request) throws Exception {
        Object loginPageObject = request.getSession().getAttribute("loginPage");
        if (loginPageObject == null) {
            return null;
        }
        LoginPagePO loginPage = (LoginPagePO) loginPageObject;

        ContactListPO contact = WeChatUtil
            .getContact(loginPage.getPassTicket(), loginPage.getSKey(), loginPage.getWxSid(), loginPage.getWxUin());

        return contact;
    }


    /**
     * 同步刷新
     *
     * 用对象不知道怎么接SyncKeyPO.list
     */
    @PostMapping("/refresh")
    public WebWxSyncPO refresh(@RequestBody SyncKeyPO syncKeyPO, HttpServletRequest request) throws Exception {

        WebWxSyncPO webWxSyncPO = new WebWxSyncPO();
        BaseResponsePO baseResponsePO = new BaseResponsePO();

        Object loginPageObject = request.getSession().getAttribute("loginPage");
        if (loginPageObject == null) {
            return null;
        }
        LoginPagePO loginPage = (LoginPagePO) loginPageObject;
        //同步检查
        JSONObject jsonObject = WeChatUtil.syncCheck(loginPage, syncKeyPO);
        int retCode = jsonObject.getInt("retcode");
        if (retCode != 0) {
            //错误
            log.info("syncCheck:{}", jsonObject);
            baseResponsePO.setRet(retCode);
            webWxSyncPO.setBaseResponse(baseResponsePO);
            //移除信息
            request.getSession().removeAttribute("loginPage");
        } else {
            log.info("请求更新消息");
            webWxSyncPO = WeChatUtil.webWxSync(loginPage, syncKeyPO);
            log.info("请求更新消息 end");
        }
        return webWxSyncPO;
    }

    /**
     * 请求图片
     */
    @GetMapping("/getImg")
    public void getImg(String prefix, String seq, String username, String msgId, String skey,
        @SessionAttribute("loginPage") LoginPagePO loginPagePO, HttpServletResponse response) {
        if (loginPagePO == null) {
            return;
        }
        String url = "https://wx2.qq.com" + prefix + "?seq=" + seq + "&skey=" + skey;
        if (username != null) {
            url += "&username=" + username;
        }
        if (msgId != null) {
            url += "&MsgID=" + msgId;
        }
        Map<String, String> headers = new HashMap<>();
        String cookie = "wxuin=" + loginPagePO.getWxUin();
        cookie += ";wxsid=" + loginPagePO.getWxSid();
        cookie += ";webwx_data_ticket=" + loginPagePO.getWebwx_data_ticket();
        headers.put("cookie", cookie);

        try (
            InputStream inputStream = HttpsUtil.get(url, null, headers, true);
            //微信返回的是编码ISO-8859-1，InputStreamReader()默认utf-8,坑
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"))
        ) {
            //这个方法可以在读写操作前先得知数据流里有多少个字节可以读取需要注意的是，如果这个方法用在从本地文件读取数据时，一般不会遇到问题，但如果是用于网络操作，就经常会遇到一些麻烦。比如，Socket通讯时，对方明明发来了1000个字节，但是自己的程序调用available()方法却只得到900，或者100，甚至是0，感觉有点莫名其妙，怎么也找不到原因。其实，这是因为网络通讯往往是间断性的，一串字节往往分几批进行发送。本地程序调用available()方法有时得到0，这可能是对方还没有响应，也可能是对方已经响应了，但是数据还没有送达本地。对方发送了1000个字节给你，也许分成3批到达，这你就要调用3次available()方法才能将数据总数全部得到。
            //int length = inputStream.available()
            char[] c = new char[1024];
            int position;
            PrintWriter writer = response.getWriter();
            while ((position = bufferedReader.read(c)) != -1) {
                writer.write(c, 0, position);
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //不能加返回？
        //return;
    }

    @PostMapping("/send")
    public SendMsgResponse sendMsg(SendMsgRequest msgRequest, @SessionAttribute("loginPage") LoginPagePO loginPagePO) {
        if (loginPagePO == null) {
            return null;
        }
        SendMsgResponse response = WeChatUtil.setSendMsg(loginPagePO, msgRequest);
        return response;
    }

    public static void main(String[] args) throws IOException {

    }

}
