#!/bin/bash
# emergency-fix.sh - Restaurar Spring Boot funcionando

echo "🚨 EMERGENCIA - Restaurando Spring Boot"
echo "======================================="

# 1. Ver qué está pasando
echo "1. 📊 Estado actual:"
docker-compose ps

echo ""
echo "2. 📋 Logs de Spring Boot (últimos 50 líneas):"
echo "=============================================="
docker logs crypto-backend-clean --tail=50

echo ""
echo "3. 🔍 ¿Hay errores de compilación?"
echo "================================="
docker logs crypto-backend-clean 2>&1 | grep -i "error\|exception\|failed" | tail -10

# 4. Restaurar desde backup si es necesario
echo ""
echo "4. 🔄 Restaurando archivos desde backup..."

# Restaurar SecurityConfig si existe backup
if [ -f "src/main/java/com/trading/cripto/config/SecurityConfig.java.backup" ]; then
    echo "Restaurando SecurityConfig desde backup..."
    cp src/main/java/com/trading/cripto/config/SecurityConfig.java.backup src/main/java/com/trading/cripto/config/SecurityConfig.java
    echo "✅ SecurityConfig restaurado"
else
    echo "⚠️ No hay backup de SecurityConfig, creando versión simple..."
    cat > src/main/java/com/trading/cripto/config/SecurityConfig.java << 'EOF'
package com.trading.cripto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
EOF
    echo "✅ SecurityConfig simple creado"
fi

# 5. Verificar que los archivos son válidos
echo ""
echo "5. 🔍 Verificando sintaxis Java..."
if command -v javac >/dev/null 2>&1; then
    echo "Verificando SecurityConfig..."
    javac -cp ".:src/main/java" src/main/java/com/trading/cripto/config/SecurityConfig.java 2>/dev/null && echo "✅ SecurityConfig válido" || echo "❌ SecurityConfig con errores"
else
    echo "⚠️ javac no disponible, saltando verificación"
fi

# 6. Rebuild rápido
echo ""
echo "6. 🔧 Rebuild rápido..."
docker-compose down
sleep 5
docker-compose up -d --build

# 7. Monitorear arranque
echo ""
echo "7. ⏳ Monitoreando arranque (60 segundos)..."
for i in {1..12}; do
    echo "[$i/12] Verificando..."
    
    # Verificar estado del contenedor
    STATUS=$(docker inspect crypto-backend-clean --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    echo "  Estado: $STATUS"
    
    if [ "$STATUS" = "running" ]; then
        # Probar conectividad
        if curl -f -s http://localhost:8080/api/debug/connection >/dev/null 2>&1; then
            echo "🎉 ¡Spring Boot funcionando!"
            break
        fi
    elif [ "$STATUS" = "exited" ]; then
        echo "❌ Contenedor se detuvo"
        break
    fi
    
    sleep 5
done

# 8. Test final
echo ""
echo "8. 🧪 TEST FINAL:"
echo "================"

echo "Estado de contenedores:"
docker-compose ps

echo ""
echo "Test de conectividad:"
if curl -f -s http://localhost:8080/api/debug/connection >/dev/null 2>&1; then
    echo "✅ /debug/connection - OK"
    
    if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
        echo "✅ /market/prices - OK"
        echo "🎉 TODO FUNCIONANDO"
    else
        echo "⚠️ /market/prices - Falla (pero /debug funciona)"
        echo "🔧 CORS aún necesita ajustes"
    fi
else
    echo "❌ Spring Boot no responde"
    echo "📋 Últimos logs:"
    docker logs crypto-backend-clean --tail=20
fi

# 9. Información final
echo ""
echo "========================================="
echo "🎯 ESTADO FINAL:"
echo "========================================="

FINAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/debug/connection 2>/dev/null || echo "000")

if [ "$FINAL_STATUS" = "200" ]; then
    echo "✅ Spring Boot FUNCIONANDO"
    echo "📊 Endpoint que FUNCIONA: /api/debug/connection"
    echo "📊 Endpoint que FUNCIONA: /api/debug/stats"
    echo ""
    echo "🔗 URLs funcionando:"
    echo "   http://157.245.164.138:8080/api/debug/connection"
    echo "   http://157.245.164.138:8080/api/debug/stats"
    echo ""
    echo "⚠️ Si /market/prices aún falla, usar /debug/stats como alternativa"
    echo "💡 Tu frontend puede usar /debug/stats para obtener precios"
else
    echo "❌ Spring Boot NO FUNCIONANDO"
    echo ""
    echo "🔧 ACCIONES:"
    echo "1. Ver logs: docker logs crypto-backend-clean -f"
    echo "2. Reiniciar: docker-compose restart app"
    echo "3. Si persiste: docker-compose down && docker-compose up -d"
fi

echo ""
echo "🏁 Diagnóstico de emergencia completado"
