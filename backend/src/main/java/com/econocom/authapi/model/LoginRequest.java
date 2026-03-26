package com.econocom.authapi.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO (Data Transfer Object) para la petición de login.
 * Recibe los datos del formulario de autenticación desde el frontend.
 *
 * Campos:
 * - email: dirección de correo electrónico del usuario
 * - password: contraseña del usuario
 */
@Schema(description = "Credenciales de acceso del usuario")
public class LoginRequest {

    /** Email del usuario (campo obligatorio en el formulario) */
    @Schema(description = "Email del usuario", example = "admin@econocom.com", required = true)
    private String email;

    /** Contraseña del usuario (campo obligatorio en el formulario) */
    @Schema(description = "Contraseña del usuario", example = "admin123", required = true)
    private String password;

    // Constructor vacío necesario para la deserialización JSON de Spring
    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters y Setters ---

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
