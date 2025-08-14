package com.trading.cripto.controller;

import com.trading.cripto.dto.LoginRequest;
import com.trading.cripto.dto.RegistrationRequest;
import com.trading.cripto.security.Authentication;
import com.trading.cripto.security.Authentication.AuthResponse;
import com.trading.cripto.service.UserService;
import com.trading.cripto.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final Authentication authService;

    @Autowired
    public AuthController(UserService userService, Authentication authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Registro de nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest request) {
        try {
            // Validaciones adicionales
            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "La contraseña debe tener al menos 6 caracteres"));
            }

            // Registrar usuario
            User newUser = userService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getNombreUsuario(),
                    request.getNombreCompleto(),
                    request.getFechaNacimiento(),
                    request.getFechaRegistro()
            );

            // Auto-login después del registro
            AuthResponse authResponse = authService.authenticate(request.getEmail(), request.getPassword());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("token", authResponse.getToken());
            response.put("userId", authResponse.getUserId());
            response.put("email", newUser.getEmail());
            response.put("nombreUsuario", newUser.getNombreUsuario());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Login de usuario
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.authenticate(request.getEmail(), request.getPassword());

            if (!authResponse.isSuccess()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", authResponse.getMessage()));
            }

            // Obtener información adicional del usuario
            User user = userService.findByEmail(request.getEmail()).orElseThrow();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("token", authResponse.getToken());
            response.put("userId", authResponse.getUserId());
            response.put("email", user.getEmail());
            response.put("nombreUsuario", user.getNombreUsuario());
            response.put("nombreCompleto", user.getNombreCompleto());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Error en la autenticación: " + e.getMessage()));
        }
    }

    /**
     * Validar token
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token no proporcionado"));
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        if (isValid) {
            Integer userId = authService.getUserIdFromToken(token);
            String email = authService.getEmailFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "userId", userId,
                    "email", email
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "message", "Token inválido o expirado"));
        }
    }

    /**
     * Logout (opcional - el cliente debe eliminar el token)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // En JWT, el logout se maneja del lado del cliente eliminando el token
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout exitoso. Por favor elimina el token del cliente."
        ));
    }

    /**
     * Obtener perfil del usuario autenticado
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            // El userId viene del filtro JWT
            Integer userId = (Integer) request.getAttribute("userId");

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Usuario no autenticado"));
            }

            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", user.getId());
            profile.put("email", user.getEmail());
            profile.put("nombreUsuario", user.getNombreUsuario());
            profile.put("nombreCompleto", user.getNombreCompleto());
            profile.put("fechaRegistro", user.getFechaRegistro());
            profile.put("fechaNacimiento", user.getFechaNacimiento());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}