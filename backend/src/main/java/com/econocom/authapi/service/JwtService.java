package com.econocom.authapi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-window}")
    private long refreshWindow;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtService inicializado — expiración: {}ms, ventana de refresco: {}ms",
                expiration, refreshWindow);
    }

    public String generateToken(String email, String name, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("name", name);
        claims.put("role", role);

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Token JWT generado para: {} (rol: {}, expira: {})", email, role, expiryDate);
        return token;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.warn("Firma JWT inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT mal formado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado para el usuario: {}", e.getClaims().getSubject());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Claims JWT vacíos o nulos: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean canRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expirationDate = claims.getExpiration();
            Date now = new Date();

            long timeToExpiry = expirationDate.getTime() - now.getTime();
            boolean canRefresh = timeToExpiry > 0 && timeToExpiry < refreshWindow;

            log.debug("canRefreshToken para {}: tiempo restante={}ms, puede refrescar={}",
                    claims.getSubject(), timeToExpiry, canRefresh);
            return canRefresh;
        } catch (ExpiredJwtException e) {
            log.warn("No se puede refrescar: token ya expirado para {}", e.getClaims().getSubject());
            return false;
        }
    }

    public String refreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        String name = (String) claims.get("name");
        String role = (String) claims.get("role");

        log.info("Token refrescado para el usuario: {}", email);
        return generateToken(email, name, role);
    }

    public long getExpiration() {
        return expiration;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
