package com.econocom.authapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación exitosa con el token JWT")
public class LoginResponse {

    @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Mensaje descriptivo del resultado", example = "Autenticación exitosa. Bienvenido, Administrador")
    private String message;

    @Schema(description = "Tipo de token", example = "Bearer")
    private String tokenType;

    @Schema(description = "Tiempo de expiración en milisegundos", example = "3600000")
    private long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(String token, String message, long expiresIn) {
        this.token = token;
        this.message = message;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

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
