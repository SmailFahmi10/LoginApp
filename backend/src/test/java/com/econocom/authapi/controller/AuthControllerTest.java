package com.econocom.authapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController.
 *
 * @SpringBootTest levanta el contexto completo (incluido Spring Security).
 * @AutoConfigureMockMvc inyecta MockMvc para hacer peticiones HTTP sin servidor real.
 * Los endpoints /api/auth/** son públicos según WebSecurityConfig, por lo que
 * no se necesita autenticación en los tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController - Tests de integración (MockMvc)")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // =============================================
    // Tests POST /api/auth/login
    // =============================================

    @Test
    @DisplayName("Login con credenciales válidas devuelve 200 y un token JWT")
    void login_CredencialesValidas_Devuelve200ConToken() throws Exception {
        String body = "{\"email\":\"admin@econocom.com\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta devuelve 401")
    void login_PasswordIncorrecto_Devuelve401() throws Exception {
        String body = "{\"email\":\"admin@econocom.com\",\"password\":\"wrongpass\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(true));
    }

    @Test
    @DisplayName("Login con email vacío devuelve 400")
    void login_EmailVacio_Devuelve400() throws Exception {
        String body = "{\"email\":\"\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El campo email es obligatorio"));
    }

    @Test
    @DisplayName("Login con password vacío devuelve 400")
    void login_PasswordVacio_Devuelve400() throws Exception {
        String body = "{\"email\":\"admin@econocom.com\",\"password\":\"\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El campo password es obligatorio"));
    }

    @Test
    @DisplayName("Login con email de usuario no registrado devuelve 401")
    void login_EmailNoRegistrado_Devuelve401() throws Exception {
        String body = "{\"email\":\"noexiste@econocom.com\",\"password\":\"admin123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(true));
    }

    @Test
    @DisplayName("Login con usuario estándar devuelve 200 y rol USER en el mensaje")
    void login_UsuarioEstandar_Devuelve200() throws Exception {
        String body = "{\"email\":\"user@econocom.com\",\"password\":\"user123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    // =============================================
    // Tests GET /api/auth/sso
    // =============================================

    @Test
    @DisplayName("GET /api/auth/sso devuelve 200 con la authUrl para que el frontend redirija")
    void sso_DevuelveAuthUrl() throws Exception {
        mockMvc.perform(get("/api/auth/sso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authUrl").isNotEmpty());
    }

    // =============================================
    // Tests GET /api/auth/sso/callback
    // =============================================

    @Test
    @DisplayName("SSO callback con código válido devuelve 200 y token")
    void ssoCallback_CodigoValido_Devuelve200ConToken() throws Exception {
        mockMvc.perform(get("/api/auth/sso/callback")
                .param("code", "AUTH_CODE_ECONOCOM_2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("SSO callback con código inválido devuelve 401")
    void ssoCallback_CodigoInvalido_Devuelve401() throws Exception {
        mockMvc.perform(get("/api/auth/sso/callback")
                .param("code", "CODIGO_INVALIDO"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SSO_INVALID_CODE"));
    }

    @Test
    @DisplayName("SSO callback sin código devuelve 400")
    void ssoCallback_SinCodigo_Devuelve400() throws Exception {
        mockMvc.perform(get("/api/auth/sso/callback"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SSO_MISSING_CODE"));
    }

    // =============================================
    // Tests GET /api/auth/sso/provider
    // =============================================

    @Test
    @DisplayName("SSO provider redirige al callback con el código válido (302)")
    void ssoProvider_ConRedirectUri_Redirige302() throws Exception {
        mockMvc.perform(get("/api/auth/sso/provider")
                .param("redirect_uri", "http://localhost:4200/sso/callback"))
                .andExpect(status().isFound()); // 302 Found
    }
}
