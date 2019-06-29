package com.wechat.jwt;

import com.wechat.global.FilterUrl;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JWTFilterConfig {

    @Bean
    public FilterRegistrationBean jwtFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new JWTFilter());
        registration.setName("jwtFilter");
        registration.addUrlPatterns("/*");
        registration.addInitParameter("excludeUrl", FilterUrl.EXCLUDE_URL);
        registration.setOrder(2);
        return registration;
    }

}
