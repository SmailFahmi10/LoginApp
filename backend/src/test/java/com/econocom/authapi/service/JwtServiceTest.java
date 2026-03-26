package com.econocom.authapi.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitarios para JwtService.
 *
 * Se usa @SpringBootTest(webEnvironment = NONE) para que Spring inyecte
 * los valores @Value (jwt.secret, jwt.expiration, jwt.refresh-window)
 * desde application.properties sin levantar el servidor web.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("JwtService - Tests unitarios")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private static final String EMAIL = "test@econocom.com";
    private static final String NAME  = "Test User";
    private static final String ROLE  = "USER";

    // =============================================
    // Tests de generateToken()
    // =============================================

    @Test
    @DisplayName("generateToken devuelve un token JWT no vacío")
    void generateToken_InputValido_DevuelveTokenNoVacio() {
        String token = jwtService.generateToken(EMAIL, NAME, ROLE);

        assertThat(token).isNotBlank();
        // Un JWT tiene 3 partes separadas por '.'
        assertThat(token.split("\\.")).hasSize(3);
    }

    // =============================================
    // Tests de validateToken()
    // =============================================

    @Test
    @DisplayName("validateToken devuelve true para un token recién generado")
    void validateToken_TokenValido_DevuelveTrue() {
        String token = jwtService.generateToken(EMAIL, NAME, ROLE);

        assertThat(jwtService.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken devuelve false para un token manipulado")
    void validateToken_TokenManipulado_DevuelveFalse() {
        String token = jwtService.generateToken(EMAIL, NAME, ROLE);
        // Alterar la firma (última parte del JWT)
        String manipulado = token.substring(0, token.lastIndexOf('.') + 1) + "firmaFalsa";

        assertThat(jwtService.validateToken(manipulado)).isFalse();
    }

    @Test
    @DisplayName("validateToken devuelve false para un string que no es JWT")
    void validateToken_TokenMalFormado_DevuelveFalse() {
        assertThat(jwtService.validateToken("esto.no.es.un.jwt")).isFalse();
    }

    // =============================================
    // Tests de getEmailFromToken()
    // =============================================

    @Test
    @DisplayName("getEmailFromToken extrae el email correcto del subject")
    void getEmailFromToken_TokenValido_DevuelveEmailCorrecto() {
        String token = jwtService.generateToken(EMAIL, NAME, ROLE);

        assertThat(jwtService.getEmailFromToken(token)).isEqualTo(EMAIL);
    }

    // =============================================
    // Tests de refreshToken()
    // =============================================

    @Test
    @DisplayName("refreshToken genera un token válido con el mismo email")
    void refreshToken_TokenValido_GeneraTokenValidoConMismoEmail() throws InterruptedException {
        String original = jwtService.generateToken(EMAIL, NAME, ROLE);
        // Esperar 1ms para que issuedAt sea diferente y el token cambie
        Thread.sleep(1);
        String refreshed = jwtService.refreshToken(original);

        assertThat(refreshed).isNotBlank();
        assertThat(jwtService.validateToken(refreshed)).isTrue();
        assertThat(jwtService.getEmailFromToken(refreshed)).isEqualTo(EMAIL);
    }

    // =============================================
    // Tests de canRefreshToken()
    // =============================================

    @Test
    @DisplayName("canRefreshToken devuelve false para un token recién generado (aún le queda mucho tiempo)")
    void canRefreshToken_TokenRecienGenerado_DevuelveFalse() {
        // Token con 1h de vida. La ventana de refresco es 5 min.
        // Como le quedan ~60 min, NO está dentro de la ventana → false.
        String token = jwtService.generateToken(EMAIL, NAME, ROLE);

        assertThat(jwtService.canRefreshToken(token)).isFalse();
    }

    // =============================================
    // Tests de getExpiration()
    // =============================================

    @Test
    @DisplayName("getExpiration devuelve un valor positivo configurado en application.properties")
    void getExpiration_DevuelveValorPositivo() {
        long expiration = jwtService.getExpiration();

        assertThat(expiration).isPositive();
    }
}
