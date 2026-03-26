package com.econocom.authapi.config;

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
import java.util.Collections;

/**
 * Configuración de seguridad de la aplicación.
 *
 * Define:
 * 1. Política CORS: permite peticiones desde http://localhost:4200 (frontend Angular)
 * 2. Rutas públicas: todos los endpoints bajo /api/auth/** son accesibles sin autenticación
 * 3. Sesiones: se deshabilita la gestión de sesiones (API stateless con JWT)
 * 4. CSRF: se deshabilita ya que usamos tokens JWT (no cookies de sesión)
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     *
     * @param http configurador de seguridad HTTP
     * @return SecurityFilterChain configurado
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

            // Configurar gestión de sesiones como STATELESS
            // No se crean ni se usan sesiones HTTP (cada petición se autentica con JWT)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    /**
     * Configuración CORS (Cross-Origin Resource Sharing).
     *
     * Permite que el frontend Angular (http://localhost:4200) realice
     * peticiones al backend (http://localhost:8080).
     *
     * Sin esta configuración, el navegador bloquearía las peticiones
     * cross-origin por política de seguridad del navegador.
     *
     * @return CorsConfigurationSource con las reglas CORS definidas
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos: solo el frontend Angular en desarrollo
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Headers permitidos en las peticiones
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",  // Para enviar el token JWT
                "Content-Type",   // Para peticiones JSON
                "Accept",         // Header estándar
                "Origin",         // Header CORS
                "X-Requested-With"
        ));

        // Headers expuestos en las respuestas (accesibles desde JavaScript)
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

        // Permitir envío de credenciales (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Tiempo en segundos que el navegador cachea la respuesta preflight
        configuration.setMaxAge(3600L);

        // Aplicar configuración CORS a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
