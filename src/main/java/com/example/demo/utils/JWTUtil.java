package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt.accessSecret}")
    private String secretAccess;
    @Value("${jwt.refreshSecret}")
    private String secretRefresh;
    @Value("${jwt.accessExpirationMs}")
    private long expirationAccess;
    @Value("${jwt.refreshExpirationMs}")
    private long expirationRefresh;
//    GENERATE TOKEN
    private String buildToken(String key, long expiration, String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(hashKey(key), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateAccessToken(String email){
        return buildToken(secretAccess, expirationAccess, email);
    }
    public String generateRefreshToken(String email){
        return buildToken(secretRefresh, expirationRefresh, email);
    }

//    VALIDATE TOKEN
    public Claims validateToken(String token,String key){
        Claims claims= Jwts.parserBuilder()
                .setSigningKey(hashKey(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }
    public Claims validateAT(String token){
        return validateToken(token,secretAccess);
    }
    public Claims validateRT(String token) {
        return validateToken(token, secretRefresh);
    }

    private Key hashKey(String secret){
        byte[] keyByte= Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyByte);
    }
}
