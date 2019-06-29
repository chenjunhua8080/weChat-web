package com.wechat.controller;

import com.wechat.annotation.SysLog;
import com.wechat.exception.MyException;
import com.wechat.global.GlobalConfig;
import com.wechat.global.ResultEnum;
import com.wechat.global.UserContext;
import com.wechat.jwt.JWTUtil;
import com.wechat.po.User;
import com.wechat.service.RedisService;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RedisService redisService;

    @SysLog("发送主题")
    @GetMapping("sendRedis/{text}")
    public String sendRedis(@PathVariable String text) {
        String userName = UserContext.getUserName();
        redisService.convertAndSend(GlobalConfig.redis_topic, text + " ----from：" + userName);
        return "success";
    }

    @GetMapping("/test/getToken")
    public String getToken() {
        return JWTUtil.createToken(new User(1, "admin"));
    }

    @GetMapping("/test/login")
    public String login(@NotNull(message = "user不能为空") User user) {
        if (user.getId() != 1) {
            throw new MyException(ResultEnum.UNAUTHORIZED);
        }
        return JWTUtil.createToken(user);
    }

}
