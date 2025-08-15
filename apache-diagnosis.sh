#!/bin/bash
# apache-diagnosis.sh - Diagnóstico específico para Apache

echo "🔍 DIAGNÓSTICO APACHE - Crypto Trading Simulator"
echo "================================================"

# 1. Verificar estado detallado de Apache
echo "📊 Estado actual de contenedores:"
docker-compose ps

echo ""
echo "🔍 Estado específico de Apache:"
docker ps --filter "name=apache" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. Verificar logs de Apache en tiempo real
echo ""
echo "📋 Logs recientes de Apache:"
docker-compose logs apache --tail=20

# 3. Test de conectividad interna
echo ""
echo "🧪 Test de conectividad interna de Apache:"
docker-compose exec apache curl -I http://localhost/ 2>/dev/null || echo "❌ Apache no responde internamente"

# 4. Test de proxy hacia Spring Boot
echo ""
echo "🔗 Test de proxy hacia Spring Boot:"
docker-compose exec apache curl -I http://app:8080/api/market/prices 2>/dev/null || echo "❌ No puede conectar con Spring Boot"

# 5. Verificar configuración de Apache
echo ""
echo "⚙️  Verificando configuración de Apache:"
docker-compose exec apache httpd -t 2>/dev/null || echo "❌ Error en configuración de Apache"

# 6. Verificar puertos y networking
echo ""
echo "🌐 Verificando puertos y red:"
echo "Puerto 80 (Apache):"
netstat -tuln | grep :80 || echo "❌ Puerto 80 no está en uso"

echo "Puerto 8081 (Apache mapeado):"
netstat -tuln | grep :8081 || echo "❌ Puerto 8081 no está en uso"

# 7. Test externo desde el host
echo ""
echo "🔄 Test externo desde el host:"
curl -I http://localhost:8081/ 2>/dev/null && echo "✅ Apache responde en puerto 8081" || echo "❌ Apache no responde en puerto 8081"

# 8. Información de red Docker
echo ""
echo "🐳 Red Docker de los contenedores:"
docker network ls | grep crypto
docker network inspect crypto-trading_crypto-network 2>/dev/null | grep -A 10 "Containers" || echo "Red no encontrada"

# 9. Soluciones sugeridas
echo ""
echo "========================================="
echo "🔧 POSIBLES SOLUCIONES:"
echo "========================================="
echo ""
echo "1. PROBLEMA DE CONFIGURACIÓN:"
echo "   docker-compose exec apache httpd -t"
echo "   # Si hay errores, corregir apache.conf"
echo ""
echo "2. PROBLEMA DE PUERTOS:"
echo "   # Cambiar puerto en docker-compose.yml de 8081:80 a 80:80"
echo "   # O usar: curl http://localhost:8081"
echo ""
echo "3. PROBLEMA DE RED:"
echo "   docker-compose down"
echo "   docker-compose up -d"
echo ""
echo "4. PROBLEMA DE PROXY:"
echo "   # Verificar que 'app' sea el nombre correcto del servicio"
echo "   docker-compose exec apache ping app"
echo ""
echo "5. FORZAR RECREACIÓN:"
echo "   docker-compose stop apache"
echo "   docker-compose rm -f apache"
echo "   docker-compose up -d apache"

# 10. Verificar diferencia entre "running" y "Up"
echo ""
echo "========================================="
echo "ℹ️  SOBRE 'running' vs 'Up':"
echo "========================================="
echo ""
echo "'running' puede indicar:"
echo "  - Contenedor iniciado pero healthcheck fallando"
echo "  - Proceso principal corriendo pero no respondiendo"
echo "  - Configuración incorrecta pero proceso activo"
echo ""
echo "'Up' indica:"
echo "  - Contenedor completamente funcional"
echo "  - Healthcheck pasando (si está configurado)"
echo "  - Servicio respondiendo correctamente"
