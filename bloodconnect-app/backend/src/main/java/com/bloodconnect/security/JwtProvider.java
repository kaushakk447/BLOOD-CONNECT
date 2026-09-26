package com.bloodconnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public String getUserEmailFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting email from JWT: {}", e.getMessage());
        }
        return null;
    }

    public Long getUserIdFromToken(String token) {
        try {
            Object userId = parseClaims(token).get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            }
            return (Long) userId;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting userId from JWT: {}", e.getMessage());
        }
        return null;
    }

    public String getRoleFromToken(String token) {
        try {
            return parseClaims(token).get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Error extracting role from JWT: {}", e.getMessage());
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }
}
