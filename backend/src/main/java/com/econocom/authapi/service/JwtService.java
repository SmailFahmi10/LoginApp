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

/**
 * Servicio responsable de la gestión de tokens JWT.
 *
 * Proporciona métodos para:
 * - Generar tokens JWT con claims personalizados y expiración configurable
 * - Validar tokens existentes (firma, expiración, formato)
 * - Extraer información (claims) de un token
 * - Determinar si un token es elegible para refresco
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** Clave secreta codificada en Base64 (se lee desde application.properties) */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración del token en milisegundos */
    @Value("${jwt.expiration}")
    private long expiration;

    /** Ventana de tiempo (ms) antes de expirar en la que se permite refrescar el token */
    @Value("${jwt.refresh-window}")
    private long refreshWindow;

    /** Clave criptográfica derivada del secret para firmar/verificar tokens */
    private Key signingKey;

    /**
     * Inicializa la clave de firma tras la inyección de dependencias.
     * Se usa HMAC-SHA256, que requiere una clave de al menos 256 bits.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtService inicializado — expiración: {}ms, ventana de refresco: {}ms",
                expiration, refreshWindow);
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * @param email email del usuario autenticado
     * @param name  nombre completo del usuario
     * @param role  rol del usuario en el sistema
     * @return token JWT firmado como String
     */
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

    /**
     * Valida un token JWT verificando firma, expiración y formato.
     *
     * @param token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
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

    /**
     * Extrae el email (subject) del token JWT.
     *
     * @param token JWT del que extraer el subject
     * @return email del usuario contenido en el token
     */
    public String getEmailFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Determina si un token es elegible para ser refrescado.
     * Un token puede refrescarse si le quedan menos de 'refreshWindow' ms para expirar.
     *
     * @param token JWT a evaluar
     * @return true si el token puede ser refrescado
     */
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

    /**
     * Refresca un token generando uno nuevo con la misma información
     * pero con una nueva fecha de expiración.
     *
     * @param token JWT actual a refrescar
     * @return nuevo token JWT con expiración renovada
     */
    public String refreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String email = claims.getSubject();
        String name = (String) claims.get("name");
        String role = (String) claims.get("role");

        log.info("Token refrescado para el usuario: {}", email);
        return generateToken(email, name, role);
    }

    /** Devuelve el tiempo de expiración configurado en milisegundos. */
    public long getExpiration() {
        return expiration;
    }

    /**
     * Extrae todos los claims del payload del token JWT.
     *
     * @param token JWT del que extraer los claims
     * @return objeto Claims con toda la información del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
