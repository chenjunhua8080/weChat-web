package com.weChat.controller;

import com.weChat.po.wechat.ContactListPO;
import com.weChat.po.wechat.InitPO;
import com.weChat.po.wechat.LoginPagePO;
import com.weChat.request.InitRequest;
import com.weChat.util.WeChatUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

@RestController
public class UserController {

    @GetMapping("/getQrCode")
    public Map<String, Object> getQrCode(@SessionAttribute(required = false) Map<String, Object> data)
        throws Exception {
        if (data != null) {
            return data;
        }
        String uuid = WeChatUtil.jsLogin();
//        String qrCode = WeChatUtil.getQrCode(uuid);
        //不生成图片了，直接返回链接
        String qrCode = WeChatUtil.qrCode.replace("UUID", uuid);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("uuid", uuid);
        resultMap.put("qrCode", qrCode);
        return resultMap;
    }

    @GetMapping("/getLoginStatus/{uuid}")
    public Map<String, Object> getLoginStatus(@PathVariable("uuid") String uuid) throws Exception {
        Map<String, Object> loginStatus = WeChatUtil.waitForLogin(0, uuid);
        return loginStatus;
    }

    @GetMapping("/init")
    public Map<String, Object> init(InitRequest initRequest, HttpServletRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        LoginPagePO loginPage = WeChatUtil.loginPage(initRequest);
        InitPO init = WeChatUtil
            .init(loginPage.getWxSid(), loginPage.getSKey(), loginPage.getWxUin(), loginPage.getPassTicket());
        ContactListPO contact = WeChatUtil
            .getContact(loginPage.getPassTicket(), loginPage.getSKey(), loginPage.getWxSid(), loginPage.getWxUin());

        result.put("init", init);
        result.put("contact", contact);

        HttpSession session = request.getSession();
        session.setAttribute("data", result);

        return result;
    }

    @GetMapping("/refresh")
    public Map<String, Object> refresh(@PathVariable("uuid") String uuid) throws Exception {
        Map<String, Object> loginStatus = WeChatUtil.waitForLogin(0, uuid);
        return loginStatus;
    }

}
