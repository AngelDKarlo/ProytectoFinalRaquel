#!/bin/bash
# fix-database.sh - Diagnóstico y reparación de la base de datos

set -e

# Colores
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

echo "🔧 DIAGNÓSTICO Y REPARACIÓN DE BASE DE DATOS"
echo "=============================================="

# 1. Verificar estado de contenedores
print_status "Verificando estado de contenedores..."
docker-compose ps

# 2. Verificar variables de entorno
print_status "Verificando variables de entorno..."
if [ -f ".env" ]; then
    print_success "✅ Archivo .env encontrado"
    echo "Contenido de .env:"
    grep -E "^[A-Z]" .env | head -10
else
    print_error "❌ Archivo .env no encontrado"
    print_warning "Creando .env desde .env.production..."
    cp .env.production .env 2>/dev/null || echo "Archivo .env.production no encontrado"
fi

# 3. Test de conectividad de la API
print_status "Probando conectividad de la API..."
API_URL="http://localhost:8080"

# Test básico
if curl -f -s "$API_URL/api/market/prices" > /dev/null; then
    print_success "✅ API respondiendo en puerto 8080"
else
    print_warning "⚠️ API no responde en puerto 8080"
fi

# Test a través de Apache
if curl -f -s "http://localhost/api/market/prices" > /dev/null; then
    print_success "✅ API respondiendo a través de Apache"
else
    print_warning "⚠️ API no responde a través de Apache"
fi

# 4. Test de conexión a base de datos
print_status "Probando conexión a base de datos..."
RESPONSE=$(curl -s "$API_URL/api/debug/connection" || echo '{"connected": false}')
echo "Respuesta de conexión: $RESPONSE"

# 5. Verificar datos en base de datos
print_status "Verificando datos en base de datos..."
STATS_RESPONSE=$(curl -s "$API_URL/api/debug/stats" || echo '{"success": false}')
echo "Estadísticas de BD: $STATS_RESPONSE"

# 6. Verificar logs de la aplicación
print_status "Verificando logs de la aplicación..."
echo "🔍 Últimos logs de la aplicación:"
docker-compose logs app --tail=20

# 7. Verificar logs de MySQL
print_status "Verificando logs de MySQL..."
echo "🔍 Últimos logs de MySQL:"
docker-compose logs mysql --tail=10

# 8. Acciones de reparación
print_status "Iniciando acciones de reparación..."

# 8.1 Reiniciar contenedores
print_status "Reiniciando contenedores..."
docker-compose down
sleep 5
docker-compose up -d

# 8.2 Esperar a que estén listos
print_status "Esperando a que los servicios estén listos..."
sleep 30

# 8.3 Forzar actualización de datos
print_status "Forzando actualización de datos..."
sleep 10
curl -X POST "$API_URL/api/debug/update-prices" || echo "No se pudo actualizar precios"

# 8.4 Test final
print_status "Realizando test final..."
sleep 5

# Test de precios
PRICES_RESPONSE=$(curl -s "$API_URL/api/market/prices" || echo '[]')
CRYPTO_COUNT=$(echo "$PRICES_RESPONSE" | grep -o '"id"' | wc -l)
echo "Número de criptomonedas encontradas: $CRYPTO_COUNT"

if [ "$CRYPTO_COUNT" -gt 0 ]; then
    print_success "✅ Base de datos funcionando correctamente"
    echo "📊 Precios actuales:"
    echo "$PRICES_RESPONSE" | head -10
else
    print_error "❌ No se encontraron datos en la base de datos"
    
    # Intentar test directo de BD
    print_status "Intentando test directo de base de datos..."
    curl -X GET "$API_URL/api/debug/test-db"
fi

# 9. Información final
echo ""
echo "========================================="
echo -e "${GREEN}🎯 RESUMEN DEL DIAGNÓSTICO${NC}"
echo "========================================="
echo ""
echo "🔗 URLs para probar:"
echo "   📊 Precios: http://157.245.164.138/api/market/prices"
echo "   🔍 Debug Stats: http://157.245.164.138/api/debug/stats"
echo "   🔧 Test DB: http://157.245.164.138/api/debug/test-db"
echo "   ❤️ Health: http://157.245.164.138/api/debug/connection"
echo ""
echo "📋 Comandos de verificación:"
echo "   docker-compose ps"
echo "   docker-compose logs app"
echo "   docker-compose logs mysql"
echo ""
echo "🔄 Si los problemas persisten:"
echo "   1. Verificar archivo .env"
echo "   2. Revisar logs: docker-compose logs"
echo "   3. Reiniciar: docker-compose restart"
echo "   4. Rebuild: docker-compose up --build -d"
echo ""

print_success "🏁 Diagnóstico completado"
