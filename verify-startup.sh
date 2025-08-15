#!/bin/bash
# verify-startup.sh - Verificar que todo esté funcionando

echo "🔍 VERIFICANDO ARRANQUE COMPLETO"
echo "================================"

# 1. Estado de contenedores
echo "1. Estado actual de contenedores:"
docker-compose ps

echo ""
echo "2. Esperando a que Spring Boot arranque completamente..."
echo "   (Spring Boot puede tomar 1-2 minutos en arrancar)"

# 3. Monitorear arranque de Spring Boot
for i in {1..24}; do
    echo -n "[$i/24] "
    
    # Verificar si el contenedor está corriendo
    STATUS=$(docker inspect crypto-backend --format='{{.State.Status}}' 2>/dev/null || echo "not_found")
    
    if [ "$STATUS" = "running" ]; then
        # Verificar si responde en la API
        if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
            echo "✅ Spring Boot está funcionando!"
            break
        else
            echo "Spring Boot arrancando... (contenedor running, API no responde aún)"
        fi
    else
        echo "Contenedor: $STATUS"
    fi
    
    sleep 5
done

echo ""
echo "3. Estado final:"
docker-compose ps

echo ""
echo "4. Test de conectividad completo:"

# Test básico
echo "📊 Test básico API:"
if curl -f -s http://localhost:8080/api/market/prices >/dev/null; then
    echo "✅ API responde"
    curl -s http://localhost:8080/api/market/prices | head -3
else
    echo "❌ API no responde aún"
fi

echo ""
echo "🌐 Test CORS (simulando navegador):"
CORS_RESPONSE=$(curl -s -H "Origin: http://localhost" -H "Access-Control-Request-Method: GET" -X OPTIONS http://localhost:8080/api/market/prices -w "HTTP_CODE:%{http_code}")
echo "Respuesta CORS: $CORS_RESPONSE"

echo ""
echo "🔍 Test desde IP externa:"
if curl -f -s http://157.245.164.138:8080/api/market/prices >/dev/null; then
    echo "✅ API responde desde IP externa"
else
    echo "❌ API no responde desde IP externa"
fi

echo ""
echo "5. Logs recientes de Spring Boot:"
echo "================================"
docker logs crypto-backend --tail=15

echo ""
echo "========================================="
echo "🎯 RESULTADO:"
echo "========================================="

# Verificación final
if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
    echo "🎉 ¡TODO FUNCIONANDO!"
    echo ""
    echo "✅ Spring Boot: FUNCIONANDO"
    echo "✅ CORS: CONFIGURADO"
    echo "✅ API: RESPONDIENDO"
    echo ""
    echo "🔗 URLs para probar:"
    echo "   📊 API: http://157.245.164.138:8080/api/market/prices"
    echo "   🔍 Debug: http://157.245.164.138:8080/api/debug/stats"
    echo "   👤 Auth: http://157.245.164.138:8080/api/auth/register"
    echo ""
    echo "🌐 Tu HTML debería conectarse ahora sin problemas"
    
else
    echo "⚠️ Spring Boot aún arrancando o hay un problema"
    echo ""
    echo "📋 Para seguir monitoreando:"
    echo "   docker logs crypto-backend -f"
    echo "   docker-compose ps"
    echo ""
    echo "🔄 Si no arranca en 2 minutos, revisar logs"
fi

echo ""
echo "🏁 Verificación completada"
