package com.example.demo.Util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenUtil {

    private String secretKey = "9Krj29fVGkP8GtbvCo7m4cWUsQdf8WbIh6tJgw5Xt9c="; // 最好存放在配置文件中

    // 生成 JWT
    public String generateToken(String username, String role, int id) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)  // 在 token 中携带 role 信息
                .claim("id", id)  // 在 token 中携带 id 信息
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))  // 设置1小时过期
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }


    // 从 JWT 获取用户名
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 验证 JWT 是否有效
    public String validateToken(String token) {
        String username = getUsernameFromToken(token);
        if(username != null && !isTokenExpired(token)){
            return username;
        }
        return null;
    }

    // 检查 JWT 是否过期
    public boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    // 获取 token 的过期时间
    private Date getExpirationDateFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
