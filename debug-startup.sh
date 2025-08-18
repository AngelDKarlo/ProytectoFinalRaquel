#!/bin/bash
# debug-startup.sh - Diagnosticar por qué Spring Boot no arranca

echo "🔍 DIAGNÓSTICO DE ARRANQUE - Spring Boot"
echo "========================================="

# 1. Estado de contenedores
echo "1. 📊 Estado actual de contenedores:"
docker-compose ps

echo ""
echo "2. 🔍 Estado detallado de la aplicación:"
docker inspect crypto-backend-clean --format='{{.State.Status}}' 2>/dev/null || echo "Contenedor no encontrado"

# 3. Logs de la aplicación
echo ""
echo "3. 📋 Logs de Spring Boot (últimos 30 líneas):"
echo "=============================================="
docker logs crypto-backend-clean --tail=30 2>&1

# 4. Logs de MySQL
echo ""
echo "4. 📋 Logs de MySQL (últimos 10 líneas):"
echo "======================================="
docker logs crypto-mysql-clean --tail=10 2>&1

# 5. Test de conectividad de red
echo ""
echo "5. 🌐 Test de conectividad de red:"
echo "================================="
echo "Test interno (localhost:8080):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://localhost:8080/api/debug/connection 2>/dev/null || echo "No responde"

echo "Test externo (157.245.164.138:8080):"
curl -s -o /dev/null -w "HTTP Code: %{http_code}\n" http://157.245.164.138:8080/api/debug/connection 2>/dev/null || echo "No responde"

# 6. Verificar puertos
echo ""
echo "6. 🔌 Puertos en uso:"
echo "===================="
ss -tulpn | grep :8080 || echo "Puerto 8080 no está en uso"
ss -tulpn | grep :3307 || echo "Puerto 3307 no está en uso"

# 7. Test de conectividad interna entre contenedores
echo ""
echo "7. 🔗 Test de conectividad interna:"
echo "=================================="
docker-compose exec mysql mysqladmin ping -h localhost 2>/dev/null && echo "✅ MySQL interno funcionando" || echo "❌ MySQL interno falló"

# 8. Verificar compilación de Java
echo ""
echo "8. 🔧 Verificar archivos JAR en el contenedor:"
echo "=============================================="
docker exec crypto-backend-clean find /app -name "*.jar" 2>/dev/null || echo "No se puede acceder al contenedor"

# 9. Verificar proceso Java
echo ""
echo "9. ☕ Procesos Java en el contenedor:"
echo "===================================="
docker exec crypto-backend-clean ps aux 2>/dev/null | grep java || echo "No hay procesos Java o contenedor inaccesible"

# 10. Información del sistema
echo ""
echo "10. 💻 Información del sistema:"
echo "=============================="
echo "Memoria disponible:"
free -h
echo ""
echo "Espacio en disco:"
df -h | grep -E "/$|/var"

echo ""
echo "========================================="
echo "🎯 DIAGNÓSTICO COMPLETADO"
echo "========================================="

# 11. Acciones sugeridas basadas en el estado
CONTAINER_STATUS=$(docker inspect crypto-backend-clean --format='{{.State.Status}}' 2>/dev/null || echo "not_found")

echo ""
echo "📋 ESTADO ACTUAL: $CONTAINER_STATUS"
echo ""

case $CONTAINER_STATUS in
    "running")
        echo "✅ Contenedor ejecutándose - verificar logs para errores de arranque"
        echo "📋 Ejecutar: docker logs crypto-backend-clean -f"
        ;;
    "exited")
        echo "❌ Contenedor se detuvo - verificar logs de error"
        echo "📋 Ejecutar: docker logs crypto-backend-clean"
        echo "🔄 Reintentar: docker-compose restart app"
        ;;
    "restarting")
        echo "🔄 Contenedor reiniciando continuamente - error de configuración"
        echo "📋 Verificar logs y configuración"
        ;;
    "not_found")
        echo "❌ Contenedor no existe"
        echo "🔄 Recrear: docker-compose up -d"
        ;;
    *)
        echo "⚠️  Estado desconocido: $CONTAINER_STATUS"
        ;;
esac

echo ""
echo "🔧 ACCIONES RECOMENDADAS:"
echo "========================"
echo "1. Ver logs completos: docker logs crypto-backend-clean -f"
echo "2. Si no arranca: docker-compose restart app"
echo "3. Si persiste: docker-compose down && docker-compose up -d"
echo "4. Último recurso: rebuild completo"

echo ""
echo "🏁 Diagnóstico completado"

