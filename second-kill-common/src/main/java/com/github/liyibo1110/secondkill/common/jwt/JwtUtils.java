package com.github.liyibo1110.secondkill.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author liyibo
 * @date 2026-06-22 16:24
 */
public final class JwtUtils {

    private JwtUtils() {}

    private static final String DEFAULT_SECRET = "default-jwt-secret-key-must-be-at-least-32-bytes";

    /** 1 day */
    private static final long DEFAULT_EXPIRATION = 24 * 60 * 60 * 1000L;

    private static SecretKey getSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 核心方法，生成JWT的token。
     */
    public static String generateToken(Long userId, String secret, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String generateToken(Long userId) {
        return generateToken(userId, DEFAULT_SECRET, DEFAULT_EXPIRATION);
    }

    public static Claims parseToken(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Claims parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET);
    }


    public static Long getUserId(String token, String secret) {
        Claims claims = parseToken(token, secret);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 核心方法：从给定的token中，尝试解析出userId值。
     */
    public static Long getUserId(String token) {
        return getUserId(token, DEFAULT_SECRET);
    }

    public static boolean isTokenExpired(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
