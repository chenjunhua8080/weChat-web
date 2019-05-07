package com.weChat.controller;

import com.weChat.util.WeChatUtil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/getQrCode")
    public Map<String, Object> getQrCode() throws Exception {
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

}
