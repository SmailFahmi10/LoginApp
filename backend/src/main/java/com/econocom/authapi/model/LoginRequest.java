package com.econocom.authapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales de acceso del usuario")
public class LoginRequest {

    @Schema(description = "Email del usuario", example = "admin@econocom.com", required = true)
    private String email;

    @Schema(description = "Contraseña del usuario", example = "admin123", required = true)
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
