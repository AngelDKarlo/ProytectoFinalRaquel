#!/bin/bash
# quick-fix.sh - Solución rápida para problemas detectados

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

echo "🔧 SOLUCIÓN RÁPIDA - Crypto Trading"
echo "=================================="

# 1. Verificar estado actual
print_status "Verificando estado de contenedores..."
docker-compose ps || docker ps

# 2. Ver logs correctamente (sin --tail que no funciona en tu versión)
print_status "Verificando logs de la aplicación..."
echo "🔍 Logs de la aplicación (últimas líneas):"
docker-compose logs app | tail -30

print_status "Verificando logs de MySQL..."
echo "🔍 Logs de MySQL (últimas líneas):"
docker-compose logs mysql | tail -20

# 3. Test directo de APIs (sin Apache por ahora)
print_status "Probando APIs directamente..."

# Test básico
echo "🧪 Test 1: Conectividad básica"
curl -s -w "Status: %{http_code}\n" http://localhost:8080/api/market/prices || echo "FALLÓ"

echo ""
echo "🧪 Test 2: Conexión a BD"
curl -s -w "Status: %{http_code}\n" http://localhost:8080/api/debug/connection || echo "FALLÓ"

echo ""
echo "🧪 Test 3: Estadísticas de BD"
curl -s http://localhost:8080/api/debug/stats || echo "FALLÓ"

# 4. Si las APIs no responden, reiniciar servicios
API_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/market/prices 2>/dev/null || echo "000")

if [ "$API_RESPONSE" != "200" ]; then
    print_warning "⚠️ API no responde correctamente, reiniciando servicios..."
    
    # Reiniciar aplicación
    docker-compose restart app
    
    print_status "Esperando 30 segundos para que Spring Boot arranque..."
    sleep 30
    
    # Test nuevamente
    API_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/market/prices 2>/dev/null || echo "000")
    
    if [ "$API_RESPONSE" = "200" ]; then
        print_success "✅ API funcionando después del reinicio"
    else
        print_error "❌ API sigue sin responder"
        echo "Mostrando logs detallados:"
        docker-compose logs app | tail -50
    fi
fi

# 5. Forzar inicialización de datos si es necesario
print_status "Forzando inicialización de datos..."
curl -X GET http://localhost:8080/api/debug/test-db 2>/dev/null | head -10 || echo "Error en test-db"

sleep 5

curl -X POST http://localhost:8080/api/debug/update-prices 2>/dev/null | head -10 || echo "Error en update-prices"

# 6. Test final de datos
print_status "Verificación final de datos..."
echo "🔍 Precios actuales:"
curl -s http://localhost:8080/api/market/prices | head -20 || echo "No se pudieron obtener precios"

# 7. Arreglar Apache (problema detectado)
print_status "Verificando configuración de Apache..."

# Verificar si Apache está corriendo
if docker-compose ps | grep apache | grep -q Up; then
    print_success "✅ Apache está corriendo"
    
    # Verificar configuración
    echo "🔍 Test de Apache:"
    curl -s -w "Status: %{http_code}\n" http://localhost/api/market/prices || echo "Apache no está redirigiendo correctamente"
    
    # Si Apache no funciona, reiniciarlo
    print_status "Reiniciando Apache..."
    docker-compose restart apache
    sleep 10
    
    # Test Apache nuevamente
    echo "🔍 Test de Apache después del reinicio:"
    curl -s -w "Status: %{http_code}\n" http://localhost/api/market/prices || echo "Apache sigue fallando"
    
else
    print_warning "⚠️ Apache no está corriendo"
    docker-compose up -d apache
fi

# 8. Información de debug
echo ""
echo "========================================="
echo -e "${BLUE}🔍 INFORMACIÓN DE DEBUG${NC}"
echo "========================================="

print_status "Estado de contenedores:"
docker-compose ps

print_status "Puertos expuestos:"
docker-compose port app 8080 2>/dev/null || echo "Puerto 8080 no expuesto"
docker-compose port apache 80 2>/dev/null || echo "Puerto 80 no expuesto"

print_status "Test de conectividad interna:"
docker-compose exec app curl -s http://localhost:8080/api/market/prices | head -5 || echo "Conectividad interna falló"

# 9. URLs para probar externamente
echo ""
echo "========================================="
echo -e "${GREEN}🌐 URLs PARA PROBAR EXTERNAMENTE${NC}"
echo "========================================="

SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "157.245.164.138")
echo "🔗 Desde tu navegador o Postman:"
echo "   📊 Precios: http://$SERVER_IP:8080/api/market/prices"
echo "   🔍 Debug: http://$SERVER_IP:8080/api/debug/stats"
echo "   🧪 Test BD: http://$SERVER_IP:8080/api/debug/test-db"
echo "   ❤️ Health: http://$SERVER_IP:8080/api/debug/connection"
echo ""
echo "📋 Si Apache funciona:"
echo "   📊 Precios: http://$SERVER_IP/api/market/prices"
echo "   🔍 Debug: http://$SERVER_IP/api/debug/stats"

echo ""
print_success "🔧 Diagnóstico completado"
