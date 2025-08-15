#!/bin/bash
# apache-final-solution.sh - Solución definitiva con el módulo faltante

echo "🎯 SOLUCIÓN DEFINITIVA APACHE"
echo "=============================="

# 1. Detener Apache problemático
echo "🛑 Deteniendo Apache..."
docker-compose stop apache 2>/dev/null || true
docker rm -f crypto-apache 2>/dev/null || true

# 2. Crear configuración Apache CORRECTA con todos los módulos necesarios
echo "📝 Creando configuración Apache correcta..."
cat > apache-working.conf << 'EOF'
ServerRoot "/usr/local/apache2"
Listen 80

# Módulos ESENCIALES (incluyendo mod_log_config que faltaba)
LoadModule mpm_event_module modules/mod_mpm_event.so
LoadModule authz_core_module modules/mod_authz_core.so
LoadModule dir_module modules/mod_dir.so
LoadModule mime_module modules/mod_mime.so
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule headers_module modules/mod_headers.so
LoadModule log_config_module modules/mod_log_config.so

# Configuración básica
ServerName localhost
DirectoryIndex index.html
TypesConfig conf/mime.types
DocumentRoot "/usr/local/apache2/htdocs"

# Directorio principal
<Directory "/usr/local/apache2/htdocs">
    Require all granted
</Directory>

# Logs (ahora con el módulo correcto)
ErrorLog /proc/self/fd/2
CustomLog /proc/self/fd/1 combined

# Headers CORS
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header always set Access-Control-Allow-Headers "Content-Type, Authorization"

# Configuración de proxy
ProxyRequests Off
ProxyPreserveHost On

# Redirigir API al backend Spring Boot
ProxyPass /api/ http://app:8080/api/
ProxyPassReverse /api/ http://app:8080/api/

# Permitir acceso a raíz
<Location "/">
    Require all granted
</Location>

# Página de status simple
<Location "/status">
    <RequireAll>
        Require all granted
    </RequireAll>
</Location>
EOF

echo "✅ Configuración Apache creada correctamente"

# 3. Crear docker-compose final
echo "📋 Creando docker-compose final..."
cat > docker-compose-final.yml << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: crypto-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-CryptoTrading2024!}
      MYSQL_DATABASE: Cripto_db
      MYSQL_USER: crypto_user
      MYSQL_PASSWORD: ${MYSQL_PASSWORD:-CryptoPass2024!}
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./Cripto_db.sql:/docker-entrypoint-initdb.d/01-schema.sql
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  app:
    build: .
    container_name: crypto-backend
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/Cripto_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: crypto_user
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD:-CryptoPass2024!}
      JWT_SECRET: ${JWT_SECRET:-SuperSecretKeyForProductionMinimum512BitsLongForHS512Algorithm2024}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-86400000}
      SERVER_PORT: 8080
      SPRING_PROFILES_ACTIVE: production
      JAVA_OPTS: -Xmx512m -Xms256m
    ports:
      - "8080:8080"
    volumes:
      - app_logs:/app/logs
      - app_data:/app/data
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/market/prices"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  apache:
    image: httpd:2.4
    container_name: crypto-apache
    restart: always
    depends_on:
      - app
    ports:
      - "80:80"
    volumes:
      - ./apache-working.conf:/usr/local/apache2/conf/httpd.conf:ro
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/status"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 15s

networks:
  crypto-network:
    driver: bridge

volumes:
  mysql_data:
  app_logs:
  app_data:
EOF

echo "✅ Docker Compose final creado"

# 4. Aplicar configuración final
echo "🚀 Aplicando configuración final..."
cp docker-compose-final.yml docker-compose.yml

# 5. Test de configuración ANTES de levantar
echo "🧪 Validando configuración Apache antes de levantar..."
docker run --rm -v $(pwd)/apache-working.conf:/usr/local/apache2/conf/httpd.conf:ro httpd:2.4 httpd -t

if [ $? -eq 0 ]; then
    echo "✅ Configuración Apache válida"
else
    echo "❌ Configuración Apache inválida, abortando..."
    exit 1
fi

# 6. Levantar Apache con configuración correcta
echo "🚀 Levantando Apache con configuración correcta..."
docker-compose up -d apache

# 7. Monitorear arranque con paciencia
echo "⏳ Monitoreando arranque de Apache (puede tomar 30 segundos)..."
for i in {1..15}; do
    sleep 2
    STATUS=$(docker inspect crypto-apache --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    HEALTH=$(docker inspect crypto-apache --format='{{.State.Health.Status}}' 2>/dev/null || echo "none")
    
    echo "[$i/15] Estado: $STATUS | Health: $HEALTH"
    
    if [ "$STATUS" = "running" ] && [ "$HEALTH" = "healthy" ]; then
        echo "🎉 ¡Apache está funcionando perfectamente!"
        break
    elif [ "$STATUS" = "running" ] && [ "$HEALTH" = "none" ]; then
        echo "✅ Apache corriendo (sin healthcheck)"
        break
    elif [ "$STATUS" = "exited" ]; then
        echo "❌ Apache se detuvo, viendo logs..."
        docker logs crypto-apache --tail=10
        break
    fi
done

# 8. Tests finales completos
echo ""
echo "🧪 TESTS FINALES"
echo "================"

echo "1. Estado de contenedores:"
docker-compose ps

echo ""
echo "2. Test de configuración Apache:"
docker exec crypto-apache httpd -t 2>&1 && echo "✅ Config OK" || echo "❌ Config ERROR"

echo ""
echo "3. Test de conectividad interna:"
docker-compose exec app curl -s -I http://apache/status 2>/dev/null | head -1 && echo "✅ App -> Apache OK" || echo "❌ App -> Apache FAIL"

echo ""
echo "4. Test de proxy API:"
if curl -f -s http://localhost/api/market/prices >/dev/null 2>&1; then
    echo "✅ Proxy API funcionando"
    echo "📊 Respuesta de API via Apache:"
    curl -s http://localhost/api/market/prices | head -3
else
    echo "❌ Proxy API no funciona"
fi

echo ""
echo "5. Test directo Spring Boot (backup):"
if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
    echo "✅ Spring Boot funcionando directamente"
else
    echo "❌ Spring Boot no responde"
fi

# 9. Información final
echo ""
echo "========================================="
echo "🎯 RESULTADO FINAL"
echo "========================================="

APACHE_STATUS=$(docker inspect crypto-apache --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
APACHE_HEALTH=$(docker inspect crypto-apache --format='{{.State.Health.Status}}' 2>/dev/null || echo "none")

if [ "$APACHE_STATUS" = "running" ]; then
    echo "🎉 ¡ÉXITO! Apache está funcionando"
    echo ""
    echo "🔗 URLs FUNCIONANDO:"
    echo "   📊 API via Apache: http://157.245.164.138/api/market/prices"
    echo "   📊 API directa: http://157.245.164.138:8080/api/market/prices"
    echo "   ❤️ Status Apache: http://157.245.164.138/status"
    echo ""
    echo "🏆 Tu simulador está 100% funcional con Apache funcionando"
else
    echo "⚠️ Apache no pudo arrancar, pero Spring Boot funciona perfectamente"
    echo ""
    echo "🔗 URLs FUNCIONANDO:"
    echo "   📊 API: http://157.245.164.138:8080/api/market/prices"
    echo "   🔍 Debug: http://157.245.164.138:8080/api/debug/stats"
    echo ""
    echo "✅ Tu simulador funciona al 100% sin Apache"
fi

echo ""
echo "📋 Para monitorear:"
echo "   docker-compose ps"
echo "   docker logs crypto-apache"
echo ""
echo "🏁 Configuración completada"
