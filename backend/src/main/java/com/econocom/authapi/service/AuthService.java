package com.econocom.authapi.service;

import com.econocom.authapi.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de autenticación que gestiona la validación de credenciales.
 *
 * Utiliza una lista de usuarios hardcodeados en memoria (sin base de datos).
 * En un entorno de producción, este servicio se conectaría a una base de datos
 * mediante JPA/MyBatis para consultar los usuarios.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Lista de usuarios registrados en el sistema (hardcodeados en memoria) */
    private final List<User> users = new ArrayList<User>();

    /**
     * Inicializa los usuarios de prueba al arrancar la aplicación.
     * Se ejecuta automáticamente después de la inyección de dependencias.
     */
    @PostConstruct
    public void initUsers() {
        users.add(new User(1L, "admin@econocom.com", "admin123", "Administrador", "ADMIN"));
        users.add(new User(2L, "user@econocom.com", "user123", "Usuario Estándar", "USER"));
        log.info("Usuarios en memoria inicializados: {} usuarios cargados", users.size());
    }

    /**
     * Autentica un usuario verificando su email y contraseña contra
     * la lista de usuarios hardcodeados.
     *
     * @param email    email proporcionado en el formulario de login
     * @param password contraseña proporcionada en el formulario de login
     * @return User si las credenciales son válidas, null si no lo son
     */
    public User authenticate(String email, String password) {
        // Rechazar credenciales nulas sin lanzar NullPointerException
        if (email == null || password == null) {
            log.warn("Intento de autenticación con credenciales nulas");
            return null;
        }
        // Recorre la lista de usuarios buscando coincidencia de email y password
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                log.debug("Autenticación correcta para el usuario: {}", email);
                return user;
            }
        }
        // Credenciales inválidas — logueamos el email pero nunca la contraseña
        log.warn("Autenticación fallida para el email: {}", email);
        return null;
    }

    /**
     * Busca un usuario por su email.
     *
     * @param email email del usuario a buscar
     * @return User si existe, null si no se encuentra
     */
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }
}
