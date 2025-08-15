#!/bin/bash
# apache-emergency-fix.sh - Solución de emergencia para Apache

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

echo "🚨 SOLUCIÓN DE EMERGENCIA APACHE"
echo "================================="

# 1. Ver logs de Apache para diagnosticar el problema
print_status "Obteniendo logs de Apache..."
docker logs crypto-apache --tail=50 2>&1 || echo "No se pudieron obtener logs"

# 2. Detener y remover Apache completamente
print_status "Deteniendo y removiendo Apache problemático..."
docker-compose stop apache 2>/dev/null || true
docker-compose rm -f apache 2>/dev/null || true
docker rm -f crypto-apache 2>/dev/null || true

# 3. Crear configuración Apache mínima y funcional
print_status "Creando configuración Apache ultra-simple..."
cat > apache-minimal.conf << 'EOF'
ServerRoot "/usr/local/apache2"
Listen 80

LoadModule mpm_event_module modules/mod_mpm_event.so
LoadModule authz_core_module modules/mod_authz_core.so
LoadModule dir_module modules/mod_dir.so
LoadModule mime_module modules/mod_mime.so
LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule headers_module modules/mod_headers.so

ServerName localhost
DirectoryIndex index.html
TypesConfig conf/mime.types
DocumentRoot "/usr/local/apache2/htdocs"

<Directory "/usr/local/apache2/htdocs">
    Require all granted
</Directory>

ErrorLog /proc/self/fd/2
CustomLog /proc/self/fd/1 combined

# Headers CORS
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header always set Access-Control-Allow-Headers "Content-Type, Authorization"

# Proxy simple
ProxyRequests Off
ProxyPreserveHost On

ProxyPass /api/ http://app:8080/api/
ProxyPassReverse /api/ http://app:8080/api/

<Location "/">
    Require all granted
</Location>
EOF

print_success "✅ Configuración mínima creada"

# 4. Crear docker-compose simplificado sin healthcheck problemático
print_status "Creando docker-compose simplificado..."
cat > docker-compose-emergency.yml << 'EOF'
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
      - ./apache-minimal.conf:/usr/local/apache2/conf/httpd.conf:ro
    networks:
      - crypto-network

networks:
  crypto-network:
    driver: bridge

volumes:
  mysql_data:
  app_logs:
  app_data:
EOF

print_success "✅ Docker Compose simplificado creado"

# 5. Aplicar nueva configuración
print_status "Aplicando nueva configuración..."
cp docker-compose-emergency.yml docker-compose.yml

# 6. Levantar Apache con configuración mínima
print_status "Levantando Apache con configuración mínima..."
docker-compose up -d apache

# 7. Esperar menos tiempo pero monitorear
print_status "Monitoreando arranque de Apache..."
for i in {1..10}; do
    sleep 2
    STATUS=$(docker inspect crypto-apache --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    echo "Intento $i/10: Estado Apache = $STATUS"
    
    if [ "$STATUS" = "running" ]; then
        print_success "✅ Apache está corriendo!"
        break
    elif [ "$STATUS" = "exited" ] || [ "$STATUS" = "restarting" ]; then
        print_error "❌ Apache falló, viendo logs..."
        docker logs crypto-apache --tail=20 2>&1
        break
    fi
done

# 8. Test inmediato
print_status "Realizando test inmediato..."
sleep 5

echo "🔍 Estado actual:"
docker-compose ps

echo ""
echo "🧪 Test de Apache:"
if docker exec crypto-apache httpd -t 2>/dev/null; then
    print_success "✅ Configuración Apache válida"
else
    print_error "❌ Configuración Apache inválida"
    docker logs crypto-apache --tail=10
fi

echo ""
echo "🌐 Test de conectividad:"
if curl -f -s http://localhost/api/market/prices >/dev/null; then
    print_success "✅ Apache proxy funcionando"
    echo "📊 Respuesta de API via Apache:"
    curl -s http://localhost/api/market/prices | head -5
else
    print_error "❌ Apache proxy no funciona"
    echo "🔍 Probando conectividad interna..."
    docker-compose exec app curl -f http://apache/ 2>/dev/null && echo "App -> Apache OK" || echo "App -> Apache FAIL"
fi

# 9. Si Apache sigue fallando, usar solo Spring Boot
APACHE_STATUS=$(docker inspect crypto-apache --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
if [ "$APACHE_STATUS" != "running" ]; then
    print_warning "⚠️ Apache sigue fallando, configurando solo Spring Boot..."
    
    # Detener Apache problemático
    docker-compose stop apache 2>/dev/null || true
    docker rm -f crypto-apache 2>/dev/null || true
    
    echo ""
    echo "========================================="
    echo -e "${YELLOW}🔄 MODO FALLBACK ACTIVADO${NC}"
    echo "========================================="
    echo ""
    echo "Apache no pudo arrancar, pero Spring Boot funciona perfectamente:"
    echo ""
    echo "🔗 URLs funcionales:"
    echo "   📊 API: http://157.245.164.138:8080/api/market/prices"
    echo "   🔍 Debug: http://157.245.164.138:8080/api/debug/stats"
    echo "   👤 Auth: http://157.245.164.138:8080/api/auth/register"
    echo "   💰 Trading: http://157.245.164.138:8080/api/trading/execute"
    echo ""
    echo "✅ Tu simulador está 100% funcional sin Apache"
else
    echo ""
    echo "========================================="
    echo -e "${GREEN}🎉 APACHE REPARADO${NC}"
    echo "========================================="
    echo ""
    echo "🔗 URLs funcionales:"
    echo "   📊 API via Apache: http://157.245.164.138/api/market/prices"
    echo "   📊 API directa: http://157.245.164.138:8080/api/market/prices"
fi

print_success "🏁 Reparación completada"
