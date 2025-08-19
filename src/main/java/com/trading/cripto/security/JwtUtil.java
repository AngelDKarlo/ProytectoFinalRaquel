package com.trading.cripto.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:TradingCryptoSecretKey2024SuperSecureKeyForJWTGeneration}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas en millisegundos
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean isValid = (extractedUsername.equals(username) && !isTokenExpired(token));
            
            System.out.println("🔍 [JwtUtil] Validando token para: " + username);
            System.out.println("🔍 [JwtUtil] Token extraído username: " + extractedUsername);
            System.out.println("🔍 [JwtUtil] Token expirado: " + isTokenExpired(token));
            System.out.println("🔍 [JwtUtil] Token válido: " + isValid);
            
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ [JwtUtil] Error validando token: " + e.getMessage());
            return false;
        }
    }

    public Integer extractUserId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Integer userId = claims.get("userId", Integer.class);
            System.out.println("🔍 [JwtUtil] UserID extraído del token: " + userId);
            return userId;
        } catch (Exception e) {
            System.err.println("❌ [JwtUtil] Error extrayendo userId: " + e.getMessage());
            return null;
        }
    }
}