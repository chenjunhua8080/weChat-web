package com.wechat.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisReceiver {

    public void receiver(String message) {
        log.info("◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆◆接收到redis订阅消息，进入receive方法：" + message + " ◆◆◆◆◆◆◆");
    }

}
