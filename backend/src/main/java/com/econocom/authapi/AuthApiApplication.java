package com.econocom.authapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot.
 * Punto de entrada de la API de autenticación.
 *
 * @SpringBootApplication combina:
 * - @Configuration: define beans de configuración
 * - @EnableAutoConfiguration: activa la autoconfiguración de Spring Boot
 * - @ComponentScan: escanea componentes en este paquete y subpaquetes
 */
@SpringBootApplication
public class AuthApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApiApplication.class, args);
    }
}
