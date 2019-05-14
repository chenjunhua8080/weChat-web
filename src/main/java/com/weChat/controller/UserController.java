package com.weChat.controller;

import com.weChat.po.wechat.BaseResponsePO;
import com.weChat.po.wechat.ContactListPO;
import com.weChat.po.wechat.InitPO;
import com.weChat.po.wechat.LoginPagePO;
import com.weChat.po.wechat.SyncKeyPO;
import com.weChat.po.wechat.WebWxSyncPO;
import com.weChat.util.WeChatUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@RestController
public class UserController {

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
     * 获取二维码链接
     */
    @GetMapping("/getQrCode")
    public Map<String, Object> getQrCode() throws Exception {
        String uuid = WeChatUtil.jsLogin();
        //不生成图片了，直接返回链接
//        String qrCode = WeChatUtil.getQrCode(uuid);
        String qrCode = WeChatUtil.qrCode.replace("UUID", uuid);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("uuid", uuid);
        resultMap.put("qrCode", qrCode);
        return resultMap;
    }

    /**
     * 刷新登录状态，成功时初始化loginPage
     */
    @GetMapping("/getLoginStatus/{uuid}")
    public Map<String, Object> getLoginStatus(@PathVariable("uuid") String uuid,
        HttpServletRequest request, HttpServletResponse response) throws Exception {

        Map<String, Object> loginStatus = WeChatUtil.waitForLogin(0, uuid);
        Object code = loginStatus.get("code");
        if (code != null) {
            if (Integer.parseInt(code.toString()) == 200) {
                LoginPagePO loginPage = WeChatUtil
                    .loginPage(loginStatus.get("ticket").toString(),
                        loginStatus.get("uuid").toString(),
                        loginStatus.get("scan").toString());
                //设置到session
                HttpSession session = request.getSession();
                session.setAttribute("loginPage", loginPage);
                //设置到cookie
                Cookie cookie1 = new Cookie("sKey", loginPage.getSKey());
                Cookie cookie2 = new Cookie("wxSid", loginPage.getWxSid());
                Cookie cookie3 = new Cookie("wxUin", loginPage.getWxUin());
                Cookie cookie4 = new Cookie("passTicket", loginPage.getPassTicket());
                response.addCookie(cookie1);
                response.addCookie(cookie2);
                response.addCookie(cookie3);
                response.addCookie(cookie4);
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

}
