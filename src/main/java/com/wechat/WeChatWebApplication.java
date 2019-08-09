package com.wechat;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan({"com.wechat.dao"})
@SpringBootApplication
@NacosPropertySource(dataId = "example", autoRefreshed = true)
public class WeChatWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeChatWebApplication.class, args);
	}
}
