package com.wechat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({"com.wechat.dao"})
@SpringBootApplication
public class WeChatWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeChatWebApplication.class, args);
	}
}
