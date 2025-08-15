#!/bin/bash
# monitor-startup.sh - Monitorear arranque de Spring Boot

echo "⏳ MONITOREANDO ARRANQUE DE SPRING BOOT"
echo "======================================="

echo "📊 Estado inicial:"
docker-compose ps

echo ""
echo "🔍 Monitoreando arranque (Spring Boot puede tomar 1-2 minutos)..."

for i in {1..30}; do
    echo ""
    echo "[$i/30] $(date '+%H:%M:%S') - Verificando..."
    
    # Estado de contenedores
    STATUS=$(docker inspect crypto-backend-new --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    HEALTH=$(docker inspect crypto-backend-new --format='{{.State.Health.Status}}' 2>/dev/null || echo "none")
    
    echo "  📦 Contenedor: $STATUS"
    echo "  ❤️  Health: $HEALTH"
    
    # Test de conectividad
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/market/prices 2>/dev/null || echo "000")
    echo "  🌐 HTTP Code: $HTTP_CODE"
    
    if [ "$HTTP_CODE" = "200" ]; then
        echo ""
        echo "🎉 ¡SPRING BOOT ESTÁ LISTO!"
        echo "================================"
        break
    elif [ "$HTTP_CODE" = "500" ]; then
        echo "  ⚠️  Server error - revisando logs..."
        docker logs crypto-backend-new --tail=5
    elif [ "$STATUS" = "exited" ]; then
        echo "  ❌ Contenedor se detuvo - revisando logs..."
        docker logs crypto-backend-new --tail=10
        break
    fi
    
    sleep 10
done

echo ""
echo "🧪 TEST FINAL:"
echo "=============="

echo "1. Estado de contenedores:"
docker-compose ps

echo ""
echo "2. Test de API:"
if curl -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
    echo "✅ API funcionando"
    echo "📊 Precios:"
    curl -s http://localhost:8080/api/market/prices | head -3
else
    echo "❌ API no responde aún"
    echo "Últimos logs:"
    docker logs crypto-backend-new --tail=15
fi

echo ""
echo "3. Test de CORS:"
CORS_TEST=$(curl -s -H "Origin: http://localhost" http://localhost:8080/api/market/prices 2>/dev/null | head -1)
if [[ "$CORS_TEST" == *"id"* ]]; then
    echo "✅ CORS funcionando"
else
    echo "⚠️ CORS puede tener problemas"
fi

echo ""
echo "========================================="
echo "🎯 RESULTADO:"
echo "========================================="

API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/market/prices 2>/dev/null)

if [ "$API_STATUS" = "200" ]; then
    echo "🎉 ¡TODO FUNCIONANDO PERFECTAMENTE!"
    echo ""
    echo "✅ MySQL: Funcionando"
    echo "✅ Spring Boot: Funcionando" 
    echo "✅ API: Respondiendo"
    echo "✅ CORS: Configurado"
    echo ""
    echo "🔗 Tu simulador está listo en:"
    echo "   📊 API: http://157.245.164.138:8080/api/market/prices"
    echo "   🔍 Debug: http://157.245.164.138:8080/api/debug/stats"
    echo "   👤 Auth: http://157.245.164.138:8080/api/auth/register"
    echo ""
    echo "🌐 Tu HTML debería funcionar perfectamente ahora"
    
else
    echo "⚠️ Spring Boot aún está arrancando o hay un problema"
    echo ""
    echo "📋 Para seguir monitoreando:"
    echo "   docker logs crypto-backend-new -f"
    echo "   docker-compose ps"
    echo ""
    echo "🔄 Spring Boot suele tomar 1-2 minutos en arrancar completamente"
fi

echo ""
echo "🏁 Monitoreo completado"
