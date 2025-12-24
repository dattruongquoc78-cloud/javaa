package com.example.exaple06.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private final String JWT_SECRET = "coffee-shop-jwt-secret-key-2024-lab06-spring-boot-strong-secret-key-512-bits-here";

    @Value("${app.jwt.expiration:86400000}")
    private long JWT_EXPIRATION_MS;

    private SecretKey getSigningKey() {
        String secret = JWT_SECRET;
        if (secret.length() < 64) {
            secret = secret + "0".repeat(64 - secret.length());
        }
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 🔑 Tạo token
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();

        // ✅ luôn có prefix ROLE_
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ Thêm hàm lấy role từ token
    public String extractRole(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("roles", String.class);
        } catch (Exception e) {
            System.err.println("❌ JWT extractRole error: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT validation error: " + e.getMessage());
            return false;
        }
    }
}
