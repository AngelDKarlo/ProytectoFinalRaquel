package com.trading.cripto.security;

import com.trading.cripto.model.User;
import com.trading.cripto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Authentication {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Autentica un usuario y genera un token JWT
     * @param email Email del usuario
     * @param password Contraseña sin encriptar
     * @return Token JWT si la autenticación es exitosa, null en caso contrario
     */
    public AuthResponse authenticate(String email, String password) {
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            return new AuthResponse(false, "Usuario no encontrado", null, null);
        }

        User user = userOpt.get();

        // Verificar contraseña
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return new AuthResponse(false, "Contraseña incorrecta", null, null);
        }

        // Generar token JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return new AuthResponse(true, "Autenticación exitosa", token, user.getId());
    }

    /**
     * Valida un token JWT
     * @param token Token a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            return jwtUtil.validateToken(token, email);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el userId de un token
     * @param token Token JWT
     * @return userId del usuario autenticado
     */
    public Integer getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    /**
     * Extrae el email de un token
     * @param token Token JWT
     * @return email del usuario autenticado
     */
    public String getEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Clase interna para respuesta de autenticación
     */
    public static class AuthResponse {
        private boolean success;
        private String message;
        private String token;
        private Integer userId;

        public AuthResponse(boolean success, String message, String token, Integer userId) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.userId = userId;
        }

        // Getters y Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
    }
}