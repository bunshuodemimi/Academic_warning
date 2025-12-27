package com.warning.warning_system.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    private String secret = "LnQW04INIjqemUT1iYXuAisSBz1owdXwRkzn7uaH4YCoJX6zI2ItDG7IWwOi64aKcdYMKIZETFKwJh+Pi0CtpA== "; // 实际项目中请放入配置文件

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10小时有效
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}