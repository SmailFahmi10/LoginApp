package com.econocom.authapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI 3.0 (Swagger UI).
 *
 * Accesible en: http://localhost:8080/swagger-ui/index.html
 * JSON spec:    http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    /**
     * Define la información general de la API y el esquema de seguridad JWT.
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Econocom Auth API")
                        .description("API de autenticación con JWT y SSO simulado. " +
                                "Credenciales de prueba: admin@econocom.com / admin123 " +
                                "o user@econocom.com / user123. " +
                                "Código SSO válido: AUTH_CODE_ECONOCOM_2024")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Econocom")
                                .url("http://localhost:4200"))
                        .license(new License()
                                .name("Prueba Técnica")))
                // Esquema de seguridad: Bearer JWT
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduce el token JWT obtenido en /api/auth/login")));
    }
}
