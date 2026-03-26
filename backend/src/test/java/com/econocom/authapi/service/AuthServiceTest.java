package com.econocom.authapi.service;

import com.econocom.authapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para AuthService.
 *
 * Se usa @InjectMocks para crear la instancia de AuthService sin Spring context.
 * initUsers() se llama manualmente en @BeforeEach porque @PostConstruct
 * no se dispara fuera del contenedor Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Tests unitarios")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Simular el @PostConstruct manualmente
        authService.initUsers();
    }

    // =============================================
    // Tests de authenticate()
    // =============================================

    @Test
    @DisplayName("Autenticar admin con credenciales correctas devuelve el usuario")
    void authenticate_CredencialesAdmin_DevuelveUsuario() {
        User result = authService.authenticate("admin@econocom.com", "admin123");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("admin@econocom.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Autenticar con contraseña incorrecta devuelve null")
    void authenticate_PasswordIncorrecto_DevuelveNull() {
        User result = authService.authenticate("admin@econocom.com", "wrongpassword");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Autenticar con email desconocido devuelve null")
    void authenticate_EmailDesconocido_DevuelveNull() {
        User result = authService.authenticate("noexiste@econocom.com", "admin123");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Autenticar con email en mayúsculas funciona (case-insensitive)")
    void authenticate_EmailEnMayusculas_DevuelveUsuario() {
        User result = authService.authenticate("ADMIN@ECONOCOM.COM", "admin123");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Administrador");
    }

    // =============================================
    // Tests de findByEmail()
    // =============================================

    @Test
    @DisplayName("Buscar usuario existente por email devuelve el usuario correcto")
    void findByEmail_EmailExistente_DevuelveUsuario() {
        User result = authService.findByEmail("user@econocom.com");

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.getName()).isEqualTo("Usuario Estándar");
    }

    @Test
    @DisplayName("Buscar usuario no registrado devuelve null")
    void findByEmail_EmailNoRegistrado_DevuelveNull() {
        User result = authService.findByEmail("fantasma@econocom.com");

        assertThat(result).isNull();
    }

    // =============================================
    // Tests de casos límite (null / vacío)
    // =============================================

    @Test
    @DisplayName("Autenticar con email null devuelve null sin lanzar excepción")
    void authenticate_EmailNull_DevuelveNull() {
        User result = authService.authenticate(null, "admin123");

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Autenticar con password null devuelve null sin lanzar excepción")
    void authenticate_PasswordNull_DevuelveNull() {
        User result = authService.authenticate("admin@econocom.com", null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Buscar con email null devuelve null sin lanzar excepción")
    void findByEmail_EmailNull_DevuelveNull() {
        User result = authService.findByEmail(null);

        assertThat(result).isNull();
    }
}
