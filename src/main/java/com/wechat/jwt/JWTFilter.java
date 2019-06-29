package com.wechat.jwt;

import com.wechat.exception.MyException;
import com.wechat.global.ResultEnum;
import com.wechat.global.UserContext;
import com.wechat.po.User;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.util.StringUtils;


public class JWTFilter implements Filter {

    private Set<String> excludeUrls;

    @Override
    public void init(FilterConfig filterConfig) {
        String excludeUrlStr = filterConfig.getInitParameter("excludeUrl");
        excludeUrls = new HashSet<>(Arrays.asList(excludeUrlStr.split(",")));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String path = request.getServletPath();
        if (1==2) {
//        if (!excludeUrls.contains(path)) {
            String requestToken = JWTUtil.getRequestToken(request);
            if (StringUtils.isEmpty(requestToken)) {
                throw new MyException(ResultEnum.NOT_LOGIN);
            }
            Claims claims = JWTUtil.parseJWT(requestToken);
            if (claims == null) {
                throw new MyException("无效token");
            }
            User user = JWTUtil.gerUser(claims);
            if (user == null) {
                throw new MyException("用户不存在");
            }
            UserContext.setUser(user);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        UserContext.clearUser();
    }

    @Override
    public void destroy() {

    }
}
