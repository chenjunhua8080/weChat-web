package com.wechat.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.wechat.global.GlobalConfig;
import com.wechat.global.UserContext;
import com.wechat.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RedisService redisService;

    @GetMapping("sendRedis/{text}")
    public String sendRedis(@PathVariable String text) {
        String userName = UserContext.getUserName();
        redisService.convertAndSend(GlobalConfig.redis_topic, text + " ----from：" + userName);
        return "success";
    }

    @GetMapping("/jenkins")
    public String jenkins() {
        return "jenkins 0727";
    }


    @NacosValue(value = "${name:null}", autoRefreshed = true)
    private String name;

    @GetMapping("/nacos")
    public String nacos() {
        return name;
    }

}
