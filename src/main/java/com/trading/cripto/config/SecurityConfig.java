// SecurityConfig.java - CORREGIDO para CORS
package com.trading.cripto.config;

import com.trading.cripto.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos - SIN autenticación
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/market/prices").permitAll()
                        .requestMatchers("/api/market/price/**").permitAll()
                        .requestMatchers("/api/market/history/**").permitAll()
                        .requestMatchers("/api/market/stats/**").permitAll()
                        .requestMatchers("/api/market/summary").permitAll()
                        .requestMatchers("/api/market/recent/**").permitAll()
                        
                        // Debug endpoints - públicos para testing
                        .requestMatchers("/api/debug/connection").permitAll()
                        .requestMatchers("/api/debug/stats").permitAll()
                        .requestMatchers("/api/debug/test-db").permitAll()
                        .requestMatchers("/api/debug/update-prices").permitAll()
                        
                        // Health check
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // OPTIONS requests para CORS
                        .requestMatchers("OPTIONS", "/**").permitAll()
                        
                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // PERMITIR TODOS LOS ORÍGENES para desarrollo
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Orígenes específicos adicionales
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200", 
            "http://localhost:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:8080",
            "null" // Para archivos locales HTML
        ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credentials
        configuration.setAllowCredentials(true);
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Cache-Control", "Content-Type"
        ));
        
        // Max age para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
