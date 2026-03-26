package com.econocom.authapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de respuesta para el endpoint de login.
 * Contiene el token JWT generado y un mensaje descriptivo.
 *
 * Se devuelve al frontend tras una autenticación exitosa.
 */
@Schema(description = "Respuesta de autenticación exitosa con el token JWT")
public class LoginResponse {

    /** Token JWT generado para el usuario autenticado */
    @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    /** Mensaje descriptivo del resultado (éxito o error) */
    @Schema(description = "Mensaje descriptivo del resultado", example = "Autenticación exitosa. Bienvenido, Administrador")
    private String message;

    /** Tipo de token (siempre "Bearer" para JWT) */
    @Schema(description = "Tipo de token", example = "Bearer")
    private String tokenType;

    /** Tiempo de expiración del token en milisegundos */
    @Schema(description = "Tiempo de expiración en milisegundos", example = "3600000")
    private long expiresIn;

    // Constructor vacío para serialización
    public LoginResponse() {
    }

    /**
     * Constructor completo para respuesta de login exitoso.
     *
     * @param token     JWT generado
     * @param message   mensaje descriptivo
     * @param expiresIn tiempo de expiración en ms
     */
    public LoginResponse(String token, String message, long expiresIn) {
        this.token = token;
        this.message = message;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    // --- Getters y Setters ---

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
