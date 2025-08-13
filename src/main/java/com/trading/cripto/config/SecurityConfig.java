package com.trading.cripto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Indica que esta es una clase de configuración
public class SecurityConfig {

    @Bean // Le dice a Spring que este método crea un "Bean" (un objeto gestionado)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}