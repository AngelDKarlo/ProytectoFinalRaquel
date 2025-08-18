package com.trading.cripto.controller;

import com.trading.cripto.model.User;
import com.trading.cripto.model.Portafolio;
import com.trading.cripto.repository.UserRepository;
import com.trading.cripto.repository.PortafolioRepository;
import com.trading.cripto.security.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortafolioRepository portafolioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Authentication authenticationService;

    /**
     * Registro de usuario con JWT real
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("📝 [AuthController] Registro recibido: " + requestData);

            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            String nombreUsuario = (String) requestData.get("nombreUsuario");
            String nombreCompleto = (String) requestData.get("nombreCompleto");
            String fechaNacimiento = (String) requestData.get("fechaNacimiento");

            // Validaciones
            if (email == null || password == null || nombreUsuario == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Faltan campos requeridos")
                );
            }

            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "La contraseña debe tener al menos 6 caracteres")
                );
            }

            // Verificar si el email ya existe
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "El email ya está registrado")
                );
            }

            // Crear nuevo usuario
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setNombreUsuario(nombreUsuario);
            newUser.setNombreCompleto(nombreCompleto != null ? nombreCompleto : nombreUsuario);
            newUser.setFechaRegistro(Date.valueOf(LocalDate.now()));
            
            if (fechaNacimiento != null && !fechaNacimiento.isEmpty()) {
                newUser.setFechaNacimiento(Date.valueOf(fechaNacimiento));
            } else {
                newUser.setFechaNacimiento(Date.valueOf("1990-01-01"));
            }

            // Guardar usuario
            User savedUser = userRepository.save(newUser);
            System.out.println("✅ Usuario guardado con ID: " + savedUser.getId());

            // Crear portafolio inicial con $10,000 USD
            Portafolio portafolio = new Portafolio(savedUser.getId(), new BigDecimal("10000.00"));
            portafolioRepository.save(portafolio);
            System.out.println("✅ Portafolio creado con $10,000 USD");

            // Generar token JWT real automáticamente
            Authentication.AuthResponse authResponse = authenticationService.authenticate(email, password);

            if (authResponse.isSuccess()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Usuario registrado exitosamente");
                response.put("userId", savedUser.getId());
                response.put("email", savedUser.getEmail());
                response.put("nombreUsuario", savedUser.getNombreUsuario());
                response.put("token", authResponse.getToken()); // Token JWT real
                response.put("timestamp", System.currentTimeMillis());

                System.out.println("✅ Registro exitoso con token JWT para: " + email);
                return ResponseEntity.ok(response);
            } else {
                System.err.println("❌ Error generando token para usuario recién registrado");
                return ResponseEntity.status(500).body(
                    Map.of("success", false, "message", "Usuario creado pero error generando token")
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Error en registro: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error interno: " + e.getMessage())
            );
        }
    }

    /**
     * Login de usuario con JWT real
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("🔐 [AuthController] Login recibido: " + requestData);

            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Email y contraseña requeridos")
                );
            }

            // Usar el servicio de autenticación real
            Authentication.AuthResponse authResponse = authenticationService.authenticate(email, password);

            if (authResponse.isSuccess()) {
                // Obtener datos del usuario para la respuesta
                User user = userRepository.findByEmail(email).get();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("userId", authResponse.getUserId());
                response.put("email", user.getEmail());
                response.put("nombreUsuario", user.getNombreUsuario());
                response.put("nombreCompleto", user.getNombreCompleto());
                response.put("token", authResponse.getToken()); // JWT REAL
                response.put("timestamp", System.currentTimeMillis());

                System.out.println("✅ Login exitoso para: " + email + " con JWT");
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Login fallido para: " + email + " - " + authResponse.getMessage());
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", authResponse.getMessage())
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("success", false, "message", "Error interno: " + e.getMessage())
            );
        }
    }

    /**
     * Verificar token JWT (útil para el frontend)
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token no proporcionado")
                );
            }

            String token = authHeader.substring(7);
            boolean isValid = authenticationService.validateToken(token);

            if (isValid) {
                Integer userId = authenticationService.getUserIdFromToken(token);
                String email = authenticationService.getEmailFromToken(token);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Token válido",
                    "userId", userId,
                    "email", email
                ));
            } else {
                return ResponseEntity.status(401).body(
                    Map.of("success", false, "message", "Token inválido")
                );
            }

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                Map.of("success", false, "message", "Error validando token")
            );
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "AuthController funcionando con JWT real");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
