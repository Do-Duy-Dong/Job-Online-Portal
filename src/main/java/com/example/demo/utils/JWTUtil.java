package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JWTUtil {
    @Value("${jwt.accessSecret}")
    private String secretAccess;
    @Value("${jwt.refreshSecret}")
    private String secretRefresh;
    @Value("${jwt.accessExpirationMs}")
    private long expirationAccess;
    @Value("${jwt.refreshExpirationMs}")
    private long expirationRefresh;

    private RedisTemplate redisTemplate;
    // GENERATE TOKEN
    private String buildToken(String key,
            long expiration,
            String email,
            String companyId,
            List<String> roles,
            List<String> permissions
            ) {
        String roleString = String.join(",", roles);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("roles", roleString);
        if (companyId != null) {
            claims.put("companyId", companyId);
        }
//        set permission for Redis whenever generate AT
        redisTemplate.opsForValue().set("permission:" +email,permissions, Duration.of(expirationAccess, ChronoUnit.SECONDS));
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(hashKey(key), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateAccessToken(String email, String companyId, List<String> roles, List<String> permissions) {
        return buildToken(secretAccess, expirationAccess, email, companyId, roles, permissions);
    }

    public String generateRefreshToken(String email, String companyId, List<String> roles, List<String> permissions) {
        return buildToken(secretRefresh, expirationRefresh, email, companyId, roles, permissions);
    }

    // VALIDATE TOKEN
    public Claims validateToken(String token, String key) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(hashKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    public Claims validateAT(String token) {
        return validateToken(token, secretAccess);
    }

    public Claims validateRT(String token) {
        return validateToken(token, secretRefresh);
    }

    private Key hashKey(String secret) {
        byte[] keyByte = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyByte);
    }
}
