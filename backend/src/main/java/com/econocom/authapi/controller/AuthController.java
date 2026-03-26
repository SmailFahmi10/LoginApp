package com.econocom.authapi.controller;

import com.econocom.authapi.model.LoginRequest;
import com.econocom.authapi.model.LoginResponse;
import com.econocom.authapi.model.User;
import com.econocom.authapi.service.AuthService;
import com.econocom.authapi.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para los endpoints de autenticación.
 *
 * Expone tres endpoints:
 * 1. POST /api/auth/login - Autenticación con email/password, devuelve JWT
 * 2. GET  /api/auth/sso   - Redirige al proveedor SSO simulado (HTTP 302)
 * 3. GET  /api/auth/sso/callback - Procesa el código de autorización SSO
 *
 * Todos los endpoints son públicos (configurados en WebSecurityConfig).
 */
@Tag(name = "Autenticación", description = "Endpoints de login con JWT y flujo SSO simulado")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /** Servicio de autenticación para validar credenciales */
    private final AuthService authService;

    /** Servicio JWT para generar y gestionar tokens */
    private final JwtService jwtService;

    /** ID del cliente SSO (desde application.properties) */
    @Value("${sso.client-id}")
    private String ssoClientId;

    /** URI de redirección tras autenticación SSO */
    @Value("${sso.redirect-uri}")
    private String ssoRedirectUri;

    /** URL base del proveedor SSO simulado */
    @Value("${sso.provider-url}")
    private String ssoProviderUrl;

    /** Código de autorización válido para SSO simulado */
    @Value("${sso.valid-code}")
    private String ssoValidCode;

    /**
     * Constructor con inyección de dependencias.
     * Spring inyecta automáticamente los servicios al crear el controlador.
     */
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // =============================================
    // ENDPOINT 1: Login con email y password
    // =============================================

    /**
     * POST /api/auth/login
     *
     * Autentica al usuario con sus credenciales (email + password).
     * Si las credenciales son válidas, genera un token JWT y lo devuelve.
     * Si no, devuelve un error 401 (Unauthorized) con mensaje descriptivo.
     *
     * @param loginRequest DTO con email y password del usuario
     * @return ResponseEntity con LoginResponse (éxito) o mapa de error (401)
     */
    @Operation(
        summary = "Login con email y contraseña",
        description = "Autentica al usuario con sus credenciales. " +
            "Credenciales válidas: admin@econocom.com/admin123 o user@econocom.com/user123"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login correcto — devuelve JWT",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Campo email o password vacío",
            content = @Content(schema = @Schema(example = "{\"error\":true,\"status\":400,\"message\":\"El campo email es obligatorio\"}"))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
            content = @Content(schema = @Schema(example = "{\"error\":true,\"status\":401,\"message\":\"Credenciales inválidas.\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        // Validar que los campos no estén vacíos
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            log.warn("Petición de login rechazada: email vacío");
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "El campo email es obligatorio");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
            log.warn("Petición de login rechazada: password vacío para {}", loginRequest.getEmail());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "El campo password es obligatorio");
        }

        log.info("Intento de login para: {}", loginRequest.getEmail());

        // Intentar autenticar al usuario con las credenciales proporcionadas
        User user = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (user == null) {
            log.warn("Login fallido — credenciales inválidas para: {}", loginRequest.getEmail());
            return buildErrorResponse(HttpStatus.UNAUTHORIZED,
                    "Credenciales inválidas. Verifique su email y contraseña.");
        }

        // Credenciales válidas: generar token JWT con los datos del usuario
        String token = jwtService.generateToken(user.getEmail(), user.getName(), user.getRole());
        log.info("Login exitoso para: {} (rol: {})", user.getEmail(), user.getRole());

        LoginResponse response = new LoginResponse(
                token,
                "Autenticación exitosa. Bienvenido, " + user.getName(),
                jwtService.getExpiration()
        );

        return ResponseEntity.ok(response);
    }

    // =============================================
    // ENDPOINT 2: Inicio de flujo SSO (Ampliación)
    // =============================================

    /**
     * GET /api/auth/sso
     *
     * Simula el inicio del flujo SSO (Single Sign-On).
     * Devuelve la URL de autorización del proveedor SSO para que el frontend
     * realice la redirección (window.location.href = authUrl).
     *
     * Este enfoque es el correcto para SPAs (Angular, React, etc.) porque
     * Angular HttpClient no puede seguir redirects 302 cross-origin por CORS.
     *
     * Parámetros incluidos en la authUrl:
     * - client_id: identificador de la aplicación
     * - redirect_uri: URI a la que el proveedor redirige tras autenticación
     * - response_type: tipo de respuesta esperada (code)
     * - scope: permisos solicitados (openid profile email)
     */
    @Operation(
        summary = "Inicio del flujo SSO",
        description = "Devuelve la URL de autorización SSO para que el frontend redirija al usuario. " +
            "Copiar el valor de 'authUrl' y abrirlo en el navegador para simular el flujo completo. " +
            "El proveedor devolverá el control a /sso/callback con el código AUTH_CODE_ECONOCOM_2024."
    )
    @ApiResponse(responseCode = "200", description = "URL de autorización SSO generada correctamente",
        content = @Content(schema = @Schema(example = "{\"authUrl\":\"http://localhost:8080/api/auth/sso/provider?client_id=econocom-app-001&redirect_uri=http://localhost:4200/sso/callback&response_type=code&scope=openid%20profile%20email\"}")))
    @GetMapping("/sso")
    public ResponseEntity<Map<String, String>> ssoRedirect() {
        // Construir URL de autorización SSO con parámetros OAuth2 estándar
        String authUrl = ssoProviderUrl
                + "?client_id=" + ssoClientId
                + "&redirect_uri=" + ssoRedirectUri
                + "&response_type=code"
                + "&scope=openid%20profile%20email";

        // Devolver la URL como JSON para que el frontend haga la redirección.
        // Angular HttpClient no puede seguir 302 cross-origin, por eso se retorna
        // la URL y el componente Angular hace: window.location.href = authUrl
        Map<String, String> body = new HashMap<String, String>();
        body.put("authUrl", authUrl);
        return ResponseEntity.ok(body);
    }

    // =============================================
    // ENDPOINT 3: Callback SSO (Ampliación)
    // =============================================

    /**
     * GET /api/auth/sso/callback?code=XXX
     *
     * Procesa la respuesta del proveedor SSO simulado.
     * Si el código de autorización es válido (coincide con el valor hardcodeado),
     * devuelve un JSON de éxito con un token simulado.
     * Si no, devuelve un JSON de error con código y mensaje descriptivo.
     *
     * @param code código de autorización recibido del proveedor SSO
     * @return ResponseEntity con resultado del callback SSO
     */
    @Operation(
        summary = "Callback del proveedor SSO",
        description = "Procesa el código de autorización devuelto por el proveedor SSO. " +
            "Código válido de prueba: AUTH_CODE_ECONOCOM_2024"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticación SSO correcta — devuelve JWT",
            content = @Content(schema = @Schema(example = "{\"success\":true,\"token\":\"...\",\"tokenType\":\"Bearer\"}"))),
        @ApiResponse(responseCode = "400", description = "Código de autorización no proporcionado"),
        @ApiResponse(responseCode = "401", description = "Código de autorización inválido")
    })
    @GetMapping("/sso/callback")
    public ResponseEntity<?> ssoCallback(
            @Parameter(description = "Código de autorización SSO", example = "AUTH_CODE_ECONOCOM_2024")
            @RequestParam(value = "code", required = false) String code) {

        // Validar que se ha recibido un código de autorización
        if (code == null || code.trim().isEmpty()) {
            log.warn("SSO callback recibido sin código de autorización");
            Map<String, Object> errorResponse = new HashMap<String, Object>();
            errorResponse.put("success", false);
            errorResponse.put("errorCode", "SSO_MISSING_CODE");
            errorResponse.put("message", "No se ha proporcionado el código de autorización SSO.");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        log.info("SSO callback recibido con código: {}", code);

        // Comprobar si el código coincide con el valor válido hardcodeado
        if (ssoValidCode.equals(code)) {
            String ssoToken = jwtService.generateToken(
                    "sso-user@econocom.com",
                    "Usuario SSO",
                    "USER"
            );
            log.info("Autenticación SSO exitosa para sso-user@econocom.com");

            Map<String, Object> successResponse = new HashMap<String, Object>();
            successResponse.put("success", true);
            successResponse.put("token", ssoToken);
            successResponse.put("tokenType", "Bearer");
            successResponse.put("message", "Autenticación SSO exitosa.");
            successResponse.put("expiresIn", jwtService.getExpiration());

            return ResponseEntity.ok(successResponse);
        } else {
            log.warn("SSO callback con código inválido: {}", code);
            Map<String, Object> errorResponse = new HashMap<String, Object>();
            errorResponse.put("success", false);
            errorResponse.put("errorCode", "SSO_INVALID_CODE");
            errorResponse.put("message", "El código de autorización SSO es inválido o ha expirado.");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // =============================================
    // ENDPOINT 4: Proveedor SSO simulado (Ampliación)
    // =============================================

    /**
     * GET /api/auth/sso/provider
     *
     * Simula el comportamiento del proveedor SSO externo.
     * En un flujo OAuth2 real, el proveedor autenticaría al usuario y
     * devolvería un código de autorización. Aquí simplemente redirige
     * al redirect_uri con el código válido hardcodeado.
     *
     * Parámetros recibidos (OAuth2 estándar):
     * - client_id, redirect_uri, response_type, scope
     *
     * @param redirectUri URI de redirección configurada en el cliente OAuth2
     * @param response    HttpServletResponse para realizar la redirección
     */
    @Operation(
        summary = "Proveedor SSO simulado",
        description = "Simula el comportamiento del proveedor SSO externo. " +
            "Recibe los parámetros OAuth2 y redirige al callback con el código válido. " +
            "Este endpoint no existe en un sistema real — lo proporciona el proveedor SSO externo."
    )
    @ApiResponse(responseCode = "302", description = "Redirección al callback Angular con código de autorización")
    @GetMapping("/sso/provider")
    public void ssoProvider(
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            HttpServletResponse response) throws IOException {

        // Usar el redirect_uri recibido o el configurado por defecto
        String callbackUri = (redirectUri != null && !redirectUri.isEmpty())
                ? redirectUri
                : ssoRedirectUri;

        // Redirigir al frontend con el código de autorización simulado
        // En un proveedor real, aquí se mostraría un formulario de login SSO
        String callbackUrl = callbackUri + "?code=" + ssoValidCode;
        response.sendRedirect(callbackUrl);
    }

    // =============================================
    // Método auxiliar para respuestas de error
    // =============================================

    /**
     * Construye una respuesta de error estandarizada.
     *
     * @param status  código HTTP de estado
     * @param message mensaje descriptivo del error
     * @return ResponseEntity con el mapa de error
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> error = new HashMap<String, Object>();
        error.put("error", true);
        error.put("status", status.value());
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
}
