package com.econocom.authapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad de la aplicación.
 *
 * Define:
 * 1. Política CORS: orígenes permitidos inyectados desde application.properties
 *    (variable de entorno CORS_ALLOWED_ORIGINS en producción)
 * 2. Rutas públicas: todos los endpoints bajo /api/auth/** son accesibles sin autenticación
 * 3. Sesiones: se deshabilita la gestión de sesiones (API stateless con JWT)
 * 4. CSRF: se deshabilita ya que usamos tokens JWT (no cookies de sesión)
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Orígenes CORS permitidos, separados por coma.
     * Valor por defecto para desarrollo: http://localhost:4200
     * En producción se sobreescribe via variable de entorno CORS_ALLOWED_ORIGINS.
     *
     * En producción el frontend se sirve desde el mismo dominio que el backend
     * (Nginx hace de reverse proxy: /api/* → backend), por lo que el navegador
     * nunca realiza peticiones cross-origin. Este valor es una salvaguarda extra.
     */
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOriginsRaw;

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CORS con la configuración definida en corsConfigurationSource()
            .cors().and()

            // Deshabilitar CSRF ya que usamos JWT (tokens en header, no cookies)
            .csrf().disable()

            // Configurar autorización de peticiones
            .authorizeRequests()
                // Permitir acceso público a todos los endpoints de autenticación
                .antMatchers("/api/auth/**").permitAll()
                // Permitir Swagger UI y OpenAPI docs sin autenticación
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // Permitir peticiones OPTIONS (necesarias para preflight CORS)
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            .and()

            // API stateless: no se crean ni se usan sesiones HTTP
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    /**
     * Configuración CORS (Cross-Origin Resource Sharing).
     *
     * Los orígenes se leen de la propiedad cors.allowed-origins para que sean
     * configurables por entorno sin recompilar. En producción el tráfico llega
     * a través del mismo dominio (Nginx reverse proxy), por lo que en la práctica
     * no habrá peticiones cross-origin desde el navegador.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parsear la lista de orígenes separada por comas
        List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
