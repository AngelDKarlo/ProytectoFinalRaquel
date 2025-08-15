#!/bin/bash
# apache-fix.sh - Solución específica para Apache

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

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

echo "🔧 REPARACIÓN APACHE - Crypto Trading Simulator"
echo "================================================"

# 1. Verificar problema actual
print_status "Verificando estado actual de Apache..."
APACHE_STATUS=$(docker-compose ps apache | tail -n +3 | awk '{print $4}')
echo "Estado actual: $APACHE_STATUS"

# 2. Detener Apache problemático
print_status "Deteniendo Apache actual..."
docker-compose stop apache

# 3. Verificar y corregir configuración
print_status "Verificando configuración de Apache..."

# Crear configuración Apache simplificada y funcional
cat > apache-simple.conf << 'EOF'
# apache-simple.conf - Configuración simplificada

ServerRoot "/usr/local/apache2"
Listen 80

# Módulos esenciales
LoadModule mpm_event_module modules/mod_mpm_event.so
LoadModule authz_core_module modules/mod_authz_core.so
LoadModule dir_module modules/mod_dir.so
LoadModule mime_module modules/mod_mime.so
LoadModule rewrite_module modules/mod_rewrite.so
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
    Options Indexes FollowSymLinks
    AllowOverride None
    Require all granted
</Directory>

# Logs
ErrorLog /usr/local/apache2/logs/error.log
CustomLog /usr/local/apache2/logs/access.log combined
LogLevel warn

# CORS Headers
Header always set Access-Control-Allow-Origin "*"
Header always set Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS"
Header always set Access-Control-Allow-Headers "Content-Type, Authorization"

# Proxy hacia Spring Boot
ProxyPreserveHost On
ProxyRequests Off

# API Proxy
ProxyPass /api/ http://app:8080/api/
ProxyPassReverse /api/ http://app:8080/api/

# Health check simple
<Location "/health">
    SetHandler server-info
    Require all granted
</Location>

# Página de inicio
<Location "/">
    Require all granted
</Location>

# Configuración de seguridad
ServerTokens Prod
ServerSignature Off
EOF

print_success "✅ Configuración simplificada creada"

# 4. Actualizar docker-compose con healthcheck
print_status "Actualizando configuración de Docker Compose..."

# Crear docker-compose con healthcheck para Apache
cat > docker-compose-fixed.yml << 'EOF'
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
      app:
        condition: service_healthy
    ports:
      - "80:80"
    volumes:
      - ./apache-simple.conf:/usr/local/apache2/conf/httpd.conf:ro
      - apache_logs:/usr/local/apache2/logs
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 10s

networks:
  crypto-network:
    driver: bridge

volumes:
  mysql_data:
  app_logs:
  app_data:
  apache_logs:
EOF

print_success "✅ Docker Compose actualizado con healthcheck"

# 5. Remover contenedor Apache problemático
print_status "Removiendo contenedor Apache problemático..."
docker-compose rm -f apache

# 6. Levantar Apache con nueva configuración
print_status "Levantando Apache con configuración corregida..."
cp docker-compose-fixed.yml docker-compose.yml
docker-compose up -d apache

# 7. Esperar y verificar
print_status "Esperando 15 segundos para que Apache arranque..."
sleep 15

# 8. Test completo
print_status "Realizando tests..."

echo "🧪 Test 1: Estado de contenedores"
docker-compose ps

echo ""
echo "🧪 Test 2: Logs de Apache"
docker-compose logs apache --tail=10

echo ""
echo "🧪 Test 3: Test de configuración interna"
docker-compose exec apache httpd -t || print_warning "Configuración con advertencias"

echo ""
echo "🧪 Test 4: Conectividad externa"
if curl -f http://localhost/api/market/prices >/dev/null 2>&1; then
    print_success "✅ Apache proxy funcionando correctamente"
else
    print_warning "⚠️ Proxy no responde, probando puerto directo..."
    if curl -f http://localhost:8080/api/market/prices >/dev/null 2>&1; then
        print_warning "Spring Boot funciona pero Apache no está proxying"
    else
        print_error "Ni Apache ni Spring Boot responden"
    fi
fi

# 9. Información final
echo ""
echo "========================================="
echo -e "${GREEN}📊 INFORMACIÓN FINAL${NC}"
echo "========================================="
echo ""
echo "🔗 URLs para probar:"
echo "   📊 Via Apache: http://157.245.164.138/api/market/prices"
echo "   📊 Directo Spring: http://157.245.164.138:8080/api/market/prices"
echo "   ❤️ Health Apache: http://157.245.164.138/health"
echo ""
echo "📋 Estado esperado:"
echo "   Apache debería mostrar 'Up (healthy)' en lugar de 'running'"
echo ""
echo "🔧 Si persisten problemas:"
echo "   1. docker-compose logs apache"
echo "   2. docker-compose exec apache httpd -t"
echo "   3. Verificar puerto 80 libre: sudo netstat -tuln | grep :80"
echo ""

# 10. Estado final
print_status "Estado final de Apache:"
docker-compose ps apache

print_success "🏁 Reparación de Apache completada"
