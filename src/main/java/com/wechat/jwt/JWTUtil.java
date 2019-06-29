package com.wechat.jwt;

import com.wechat.po.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

public class JWTUtil {

    private final static long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;

    private final static String SIGN_KEY = "qwertyuiop";

    /**
     * 获取请求token
     */
    public static String getRequestToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token == null) {
            token = request.getParameter("token");
        }
        return token;
    }

    /**
     * 生成token
     */
    public static String createToken(User user) {
        JwtBuilder builder = Jwts.builder();
        builder.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        builder.setSubject(String.valueOf(user.getId()));
        builder.setIssuer("host");
        builder.setIssuedAt(new Date());
        builder.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));
        builder.claim("userId", user.getId());
        builder.claim("userName", user.getName());
        builder.signWith(SignatureAlgorithm.HS256, SIGN_KEY);
        return builder.compact();
    }

    /**
     * 解析jwt
     */
    public static Claims parseJWT(String jwtStr) {
        JwtParser parser = Jwts.parser();
        parser.setSigningKey(SIGN_KEY);
        Jwt jwt;
        try {
            jwt = parser.parse(jwtStr);
        } catch (Exception e) {
            return null;
        }
        return (Claims) jwt.getBody();
    }

    /**
     * 解析jwt，获得登录用户信息
     */
    public static User gerUser(Claims claims) {
        Integer userId = claims.get("userId", Integer.class);
        String userName = claims.get("userName", String.class);
        User user = new User();
        user.setId(userId);
        user.setName(userName);
        return user;
    }

}
