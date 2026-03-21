package com.spmss.intelliswine.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(UserDetails userDetails) {

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .signWith(signingKey)
            .compact();
    }

    public String extractSubject(String token) {
        try {

            return extractClaims(token).getSubject();

        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {

        String subject = extractSubject(token);

        if (subject == null) {
            return false;
        }

        return subject.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }

    private boolean isTokenExpired(String token) {
        try {

            Date expiration = extractClaims(token).getExpiration();

            return expiration.before(new Date());

        } catch (Exception ex) {
            return true;
        }
    }

    private Claims extractClaims(String token) {
        
        return Jwts.parserBuilder()
            .setSigningKey(signingKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
