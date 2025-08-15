#!/bin/bash
# apache-deep-debug.sh - Diagnóstico profundo de Apache

echo "🔍 DIAGNÓSTICO PROFUNDO DE APACHE"
echo "=================================="

# 1. Ver logs detallados de Apache para encontrar el error exacto
echo "📋 LOGS COMPLETOS DE APACHE:"
echo "============================"
docker logs crypto-apache --tail=100 2>&1

echo ""
echo "📋 LOGS EN TIEMPO REAL (últimos 50):"
echo "====================================="
docker logs crypto-apache -f --tail=50 &
LOG_PID=$!
sleep 10
kill $LOG_PID 2>/dev/null

echo ""
echo "🔍 ANÁLISIS DEL PROBLEMA:"
echo "========================="

# 2. Verificar si curl está disponible en el contenedor Apache
echo "1. Verificando si curl está disponible en Apache:"
docker exec crypto-apache which curl 2>/dev/null && echo "✅ curl disponible" || echo "❌ curl NO disponible - ESTE ES EL PROBLEMA"

# 3. Verificar el healthcheck específico
echo ""
echo "2. Verificando healthcheck:"
docker inspect crypto-apache | grep -A 10 -B 5 "Healthcheck"

# 4. Verificar si Apache puede hacer el healthcheck internamente
echo ""
echo "3. Probando healthcheck manual:"
docker exec crypto-apache ls -la /usr/bin/ | grep curl || echo "curl no encontrado en /usr/bin/"
docker exec crypto-apache ls -la /bin/ | grep curl || echo "curl no encontrado en /bin/"

# 5. Ver que está corriendo dentro del contenedor
echo ""
echo "4. Procesos dentro del contenedor Apache:"
docker exec crypto-apache ps aux 2>/dev/null || echo "ps no disponible"

# 6. Verificar si Apache está realmente funcionando internamente
echo ""
echo "5. Test de Apache interno sin curl:"
docker exec crypto-apache cat /proc/net/tcp | grep ":0050" && echo "✅ Apache escuchando en puerto 80" || echo "❌ Apache NO escuchando"

# 7. Verificar archivos de configuración dentro del contenedor
echo ""
echo "6. Configuración dentro del contenedor:"
docker exec crypto-apache head -10 /usr/local/apache2/conf/httpd.conf 2>/dev/null || echo "No se puede leer configuración"

echo ""
echo "========================================="
echo "🎯 POSIBLES CAUSAS DEL PROBLEMA:"
echo "========================================="
echo ""
echo "1. CURL NO DISPONIBLE: La imagen httpd:2.4 NO incluye curl por defecto"
echo "   - El healthcheck falla porque no puede ejecutar 'curl'"
echo "   - Docker reinicia el contenedor por healthcheck fallido"
echo ""
echo "2. HEALTHCHECK INCORRECTO: Necesitamos cambiar el healthcheck"
echo "   - Usar wget en lugar de curl (disponible en httpd)"
echo "   - O eliminar el healthcheck completamente"
echo ""
echo "3. CONFIGURACIÓN DE RED: Problemas de conectividad interna"
echo ""
echo "========================================="
echo "🔧 SOLUCIONES PROPUESTAS:"
echo "========================================="
echo ""
echo "SOLUCIÓN 1: Usar wget en lugar de curl"
echo "SOLUCIÓN 2: Crear imagen personalizada con curl"
echo "SOLUCIÓN 3: Eliminar healthcheck de Apache"
echo "SOLUCIÓN 4: Usar httpd -t como healthcheck"
