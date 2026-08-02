package org.example.hospitalmanagementsystem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component

public class JwtUtil {
    private static final String ROLE_CLAIM = "roles";
    private static final String TYPE_CLAIM = "type";

    public static final String ACCESS = "ACCESS";
    public static final String REFRESH = "REFRESH";
    private final SecretKey key;
    private final long expiration;
    private final long refreshExpiration;
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh.expiry}") long refreshExpiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }
    public String generateToken(Long userId, String roles) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }
    public String generateRefreshToken(Long userId  ,String roles){
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration ))
                .signWith(key)
                .compact();
    }

    // 🔍 Extract all claims (validates signature + expiry)
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(extractAllClaims(token).getSubject());
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(ROLE_CLAIM, String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TYPE_CLAIM, String.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS.equals(extractTokenType(token));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH.equals(extractTokenType(token));
    }
    public long getRefreshExpiration() {
        return refreshExpiration;
    }

    public boolean isValidToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
    }


}
