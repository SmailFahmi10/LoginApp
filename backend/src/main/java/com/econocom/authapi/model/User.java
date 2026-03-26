package com.econocom.authapi.model;

/**
 * Modelo de usuario para la autenticación en memoria.
 * Representa los datos de un usuario del sistema.
 *
 * No requiere base de datos: los usuarios se definen como
 * constantes hardcodeadas en AuthService.
 */
public class User {

    /** Identificador único del usuario */
    private Long id;

    /** Email del usuario (se usa como username para autenticación) */
    private String email;

    /** Contraseña del usuario (texto plano para esta prueba técnica) */
    private String password;

    /** Nombre completo del usuario */
    private String name;

    /** Rol del usuario en el sistema */
    private String role;

    // Constructor vacío
    public User() {
    }

    /**
     * Constructor completo para crear un usuario hardcodeado.
     *
     * @param id       identificador único
     * @param email    correo electrónico
     * @param password contraseña
     * @param name     nombre completo
     * @param role     rol del usuario
     */
    public User(Long id, String email, String password, String name, String role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
