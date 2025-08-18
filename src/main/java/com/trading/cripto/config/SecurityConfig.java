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
                // CORS PRIMERO
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilitar CSRF
                .csrf(csrf -> csrf.disable())
                
                // Sin sesiones - usar JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Configuración de autorización
                .authorizeHttpRequests(authz -> authz
                        // ENDPOINTS PÚBLICOS - SIN autenticación
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/market/**").permitAll() 
                        .requestMatchers("/api/debug/**").permitAll()
                        
                        // Health check y actuator
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health").permitAll()
                        
                        // OPTIONS requests para CORS preflight
                        .requestMatchers("OPTIONS", "/**").permitAll()
                        
                        // ✅ ENDPOINTS QUE REQUIEREN AUTENTICACIÓN JWT
                        .requestMatchers("/api/trading/**").authenticated()
                        .requestMatchers("/api/portafolio/**").authenticated()
                        
                        // Todos los demás por defecto requieren autenticación
                        .anyRequest().authenticated()
                )
                
                // Agregar filtro JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // PERMITIR TODOS LOS ORÍGENES para desarrollo local
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.addAllowedOrigin("*");
        configuration.addAllowedOrigin("file://");
        configuration.addAllowedOrigin("null"); // Para archivos HTML locales
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Headers permitidos - INCLUIR Authorization para JWT
        configuration.setAllowedHeaders(Arrays.asList(
            "*", "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"
        ));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "X-Total-Count"
        ));
        
        // CREDENTIALS FALSE para evitar conflictos con *
        configuration.setAllowCredentials(false);
        
        // Max age para preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
