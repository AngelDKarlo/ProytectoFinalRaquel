package com.trading.cripto.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        
        // Debug logging
        System.out.println("🔍 [JwtFilter] Request URI: " + requestURI);
        System.out.println("🔍 [JwtFilter] Authorization Header: " + 
            (authorizationHeader != null ? "Bearer [TOKEN]" : "NULL"));

        String email = null;
        String jwt = null;
        Integer userId = null;

        // Extraer token del header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(jwt);
                userId = jwtUtil.extractUserId(jwt);
                System.out.println("🔍 [JwtFilter] Token extraído - Email: " + email + ", UserID: " + userId);
            } catch (Exception e) {
                System.err.println("❌ [JwtFilter] Error extrayendo datos del token: " + e.getMessage());
            }
        }

        // Validar token y establecer autenticación
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwt, email)) {
                    System.out.println("✅ [JwtFilter] Token válido para: " + email);
                    
                    // Crear objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Guardar userId en el request para uso posterior
                    request.setAttribute("userId", userId);
                    request.setAttribute("userEmail", email);

                    // Establecer autenticación en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    System.out.println("✅ [JwtFilter] Autenticación establecida para userId: " + userId);
                } else {
                    System.err.println("❌ [JwtFilter] Token inválido para: " + email);
                }
            } catch (Exception e) {
                System.err.println("❌ [JwtFilter] Error validando token: " + e.getMessage());
            }
        } else if (authorizationHeader == null && requestURI.startsWith("/api/trading")) {
            System.err.println("❌ [JwtFilter] No se proporcionó token para endpoint protegido: " + requestURI);
        }

        filterChain.doFilter(request, response);
    }
}
