package com.econocom.authapi.service;

import com.econocom.authapi.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final List<User> users = new ArrayList<User>();

    @PostConstruct
    public void initUsers() {
        users.add(new User(1L, "admin@econocom.com", "admin123", "Administrador", "ADMIN"));
        users.add(new User(2L, "user@econocom.com", "user123", "Usuario Estándar", "USER"));
        log.info("Usuarios en memoria inicializados: {} usuarios cargados", users.size());
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) {
            log.warn("Intento de autenticación con credenciales nulas");
            return null;
        }
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                log.debug("Autenticación correcta para el usuario: {}", email);
                return user;
            }
        }
        log.warn("Autenticación fallida para el email: {}", email);
        return null;
    }

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
