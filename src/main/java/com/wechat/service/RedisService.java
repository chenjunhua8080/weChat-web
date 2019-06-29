package com.wechat.service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 默认过期时长，1天，单位：秒
     */
    public final static long DEFAULT_EXPIRE = 60 * 60 * 24;
    /**
     * 不设置过期时长
     */
    public final static long NOT_EXPIRE = -1;

    public void set(String key, Object value, long expire) {
        stringRedisTemplate.opsForValue().set(key, value.toString(), expire, TimeUnit.SECONDS);
    }

    public void set(String key, Object value) {
        set(key, value, DEFAULT_EXPIRE);
    }

    public <T> T get(String key, Class<T> clazz, long expire) {
        Object value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? null : (T) value;
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, NOT_EXPIRE);
    }


    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public void convertAndSend(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }
}
