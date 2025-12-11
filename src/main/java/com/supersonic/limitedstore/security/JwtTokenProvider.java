package com.supersonic.limitedstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.security.KeyStore;
import java.util.Date;
import java.util.UUID;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    // 시크릿 키
    private final String secretKey = "this-is-jwt-secret-key-very-long-123456789";

    // 토큰 만료 시간
    private final long accessTokenValidTime = 1000L * 60 * 60; // 1시간

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 생성
    public String createToken(UUID memberId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidTime);

        return Jwts.builder()
            .claim("memberId", memberId.toString())
            .claim("email", email)
            .setIssuedAt(now) // 토큰 발급 시간
            .setExpiration(expiry) // 만료시간
            .signWith(key, SignatureAlgorithm.HS256) // HS256 알고리즘으로 서명
            .compact();
    }

    // 토큰에서 email 가져오기
    public String getEmail(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return claims.getSubject();
    }

    // 토큰 유효성 검증
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
