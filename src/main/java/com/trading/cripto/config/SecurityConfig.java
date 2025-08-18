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
                // CORS PRIMERO - MUY IMPORTANTE
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilitar CSRF
                .csrf(csrf -> csrf.disable())
                
                // Sin sesiones
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
                        
                        // Todos los demás requieren autenticación
                        .anyRequest().authenticated()
                )
                
                // Agregar filtro JWT DESPUÉS de configurar CORS
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // PERMITIR TODOS LOS ORÍGENES (incluyendo file://)
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.addAllowedOrigin("*");
    configuration.addAllowedOrigin("file://");
    configuration.addAllowedOrigin("null"); // Para archivos locales
    
    // Métodos HTTP permitidos
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
    ));
    
    // Headers permitidos
    configuration.setAllowedHeaders(Arrays.asList("*"));
    
    // CREDENTIALS FALSE para evitar conflictos
    configuration.setAllowCredentials(false);
    
    // Max age para preflight requests
	 configuration.setMaxAge(3600L);

    	 UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
   	 source.registerCorsConfiguration("/**", configuration);
    
   	 return source;
	}
}
