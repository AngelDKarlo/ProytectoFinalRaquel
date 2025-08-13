package com.trading.cripto.controller;

import com.trading.cripto.dto.RegistrationRequest;
import com.trading.cripto.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 1. Indica que esta clase manejará peticiones HTTP REST
@RestController
@RequestMapping("/api/auth") // 2. Todas las rutas de este controlador empezarán con /api/auth
public class AuthController {

    private final UserService userService;

    // 3. Inyectamos el servicio
    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // 4. Mapea este método a peticiones POST a /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            // Pasamos los datos del request al servicio
            userService.registerUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getNombreUsuario(),
                    request.getNombreCompleto(),
                    request.getFechaRegistro(),
                    request.getFechaNacimiento()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

