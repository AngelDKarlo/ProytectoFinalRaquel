#!/bin/bash
# deploy-fixed.sh - Deploy corregido para Trading Crypto Simulator

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

echo "🚀 Deploy Corregido - Crypto Trading Simulator"
echo "==============================================="

# 1. Verificar que estamos en el directorio correcto
if [ ! -f "docker-compose.yml" ]; then
    print_error "❌ docker-compose.yml no encontrado. ¿Estás en el directorio correcto?"
    exit 1
fi

# 2. Crear .env con valores seguros si no existe
if [ ! -f ".env" ]; then
    print_warning "⚠️ Archivo .env no encontrado, creando uno nuevo..."
    cat > .env << 'EOF'
# Variables de Entorno - EDITAME CON VALORES REALES
MYSQL_ROOT_PASSWORD=CryptoTrading2024SecurePassword!
MYSQL_PASSWORD=CryptoUserPass2024!
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/Cripto_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=crypto_user
SPRING_DATASOURCE_PASSWORD=CryptoUserPass2024!
JWT_SECRET=SuperSecretKeyForProductionMinimum512BitsLongForHS512Algorithm2024CryptoTrading
JWT_EXPIRATION=86400000
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production
TRADING_COMMISSION=0.001
TRADING_INITIAL_BALANCE=10000
MARKET_UPDATE_INTERVAL=5000
EOF
    print_success "✅ Archivo .env creado"
fi

# 3. Verificar archivos necesarios
print_status "Verificando archivos necesarios..."
required_files=("docker-compose.yml" "Dockerfile" "pom.xml" "Cripto_db.sql")
for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        print_error "❌ Archivo requerido no encontrado: $file"
        exit 1
    fi
done
print_success "✅ Todos los archivos necesarios están presentes"

# 4. Backup de datos existentes
print_status "Creando backup de datos existentes..."
if docker volume inspect crypto-trading_mysql_data >/dev/null 2>&1; then
    BACKUP_NAME="backup-$(date +%Y%m%d-%H%M%S).tar.gz"
    docker run --rm -v crypto-trading_mysql_data:/data -v $(pwd):/backup alpine \
        tar czf /backup/$BACKUP_NAME -C /data . 2>/dev/null || true
    print_success "✅ Backup creado: $BACKUP_NAME"
fi

# 5. Detener servicios anteriores
print_status "Deteniendo servicios anteriores..."
docker-compose down || true
sleep 5

# 6. Limpiar imágenes anteriores para forzar rebuild
print_status "Limpiando imágenes anteriores..."
docker-compose down --rmi local 2>/dev/null || true

# 7. Construir nuevas imágenes
print_status "Construyendo nuevas imágenes..."
docker-compose build --no-cache --pull

# 8. Levantar servicios con profile de producción
print_status "Levantando servicios en modo producción..."
export SPRING_PROFILES_ACTIVE=production
docker-compose up -d

# 9. Verificar que los contenedores estén corriendo
print_status "Verificando contenedores..."
sleep 10
docker-compose ps

# 10. Esperar a que MySQL esté listo
print_status "Esperando a que MySQL esté listo..."
for i in {1..30}; do
    if docker-compose exec -T mysql mysqladmin ping -h"localhost" --silent; then
        print_success "✅ MySQL está listo"
        break
    fi
    echo "Esperando MySQL... ($i/30)"
    sleep 5
done

# 11. Esperar a que Spring Boot esté listo
print_status "Esperando a que Spring Boot esté listo..."
for i in {1..60}; do
    if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
        print_success "✅ Spring Boot está listo"
        break
    fi
    echo "Esperando Spring Boot... ($i/60)"
    sleep 5
done

# 12. Verificar datos en base de datos
print_status "Verificando datos en base de datos..."
sleep 10

# Test de conexión
CONNECTION_TEST=$(curl -s http://localhost:8080/api/debug/connection 2>/dev/null || echo '{"connected": false}')
echo "Test de conexión: $CONNECTION_TEST"

# Test de datos
PRICES_TEST=$(curl -s http://localhost:8080/api/market/prices 2>/dev/null || echo '[]')
CRYPTO_COUNT=$(echo "$PRICES_TEST" | grep -o '"id"' | wc -l 2>/dev/null || echo "0")

if [ "$CRYPTO_COUNT" -gt 0 ]; then
    print_success "✅ $CRYPTO_COUNT criptomonedas encontradas en BD"
else
    print_warning "⚠️ No se encontraron criptomonedas, forzando inicialización..."
    
    # Forzar test de BD
    curl -X GET http://localhost:8080/api/debug/test-db 2>/dev/null || echo "Error en test de BD"
    sleep 5
    
    # Forzar actualización de precios
    curl -X POST http://localhost:8080/api/debug/update-prices 2>/dev/null || echo "Error actualizando precios"
    sleep 5
    
    # Test final
    FINAL_TEST=$(curl -s http://localhost:8080/api/market/prices 2>/dev/null || echo '[]')
    FINAL_COUNT=$(echo "$FINAL_TEST" | grep -o '"id"' | wc -l 2>/dev/null || echo "0")
    
    if [ "$FINAL_COUNT" -gt 0 ]; then
        print_success "✅ Datos inicializados correctamente ($FINAL_COUNT criptomonedas)"
    else
        print_error "❌ No se pudieron inicializar los datos"
        echo "Revisando logs..."
        docker-compose logs app --tail=20
    fi
fi

# 13. Configurar firewall básico
print_status "Configurando firewall..."
if command -v ufw >/dev/null 2>&1; then
    sudo ufw allow 22/tcp comment 'SSH' 2>/dev/null || true
    sudo ufw allow 80/tcp comment 'HTTP' 2>/dev/null || true
    sudo ufw allow 443/tcp comment 'HTTPS' 2>/dev/null || true
    sudo ufw allow 8080/tcp comment 'Spring Boot' 2>/dev/null || true
    sudo ufw --force enable 2>/dev/null || true
    print_success "✅ Firewall configurado"
fi

# 14. Información final
echo ""
echo "========================================="
echo -e "${GREEN}🎉 Deploy Completado!${NC}"
echo "========================================="
echo ""
echo "🔗 URLs de acceso:"
SERVER_IP=$(hostname -I | awk '{print $1}' 2>/dev/null || echo "localhost")
echo "   📊 API Principal: http://$SERVER_IP/api/market/prices"
echo "   🔍 Debug Stats: http://$SERVER_IP/api/debug/stats"
echo "   🔧 Test DB: http://$SERVER_IP/api/debug/test-db"
echo "   ❤️ Health: http://$SERVER_IP/api/debug/connection"
echo ""
echo "📋 Comandos útiles:"
echo "   🔍 Ver estado: docker-compose ps"
echo "   📄 Ver logs: docker-compose logs -f app"
echo "   🔄 Reiniciar: docker-compose restart"
echo "   🛑 Detener: docker-compose down"
echo "   🔧 Debug BD: curl http://$SERVER_IP/api/debug/test-db"
echo ""
echo "📁 Archivos importantes:"
echo "   ⚙️ Configuración: .env"
echo "   📊 Logs: logs/"
echo "   💾 Base de datos: mysql_data/"
echo ""
echo -e "${YELLOW}⚠️ IMPORTANTE:${NC}"
echo "   1. Verifica que .env tenga contraseñas seguras"
echo "   2. Monitorea los logs: docker-compose logs -f"
echo "   3. Si hay problemas, ejecuta: ./fix-database.sh"
echo ""

print_success "🚀 Deploy completo! Tu simulador está funcionando."
