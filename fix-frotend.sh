#!/bin/bash
# fix-frontend-issues.sh - Solucionar problemas del frontend

echo "🔧 SOLUCIONANDO PROBLEMAS DEL FRONTEND"
echo "======================================"

echo "1. 🧪 Test directo de endpoints que usa el frontend:"
echo "===================================================="

echo "📊 Test de precios (que falla en frontend):"
curl -v -H "Origin: http://localhost" \
     -H "Access-Control-Request-Method: GET" \
     http://157.245.164.138:8080/api/market/prices 2>&1 | head -15

echo ""
echo "🔍 Respuesta de precios:"
curl -s http://157.245.164.138:8080/api/market/prices | head -10

echo ""
echo "👤 Test de autenticación (registro):"
curl -v -X POST \
     -H "Content-Type: application/json" \
     -H "Origin: http://localhost" \
     -d '{"email":"test2@test.com","nombreUsuario":"test2","nombreCompleto":"Test User","password":"123456","fechaNacimiento":"1990-01-01","fechaRegistro":"2025-08-15"}' \
     http://157.245.164.138:8080/api/auth/register 2>&1 | head -15

echo ""
echo "2. 🔍 Verificar logs de Spring Boot para errores:"
echo "================================================="
echo "Últimos logs (buscando errores):"
docker logs crypto-backend --tail=30 | grep -i "error\|exception\|fail" || echo "No hay errores evidentes en logs"

echo ""
echo "3. 🌐 Verificar CORS específicamente:"
echo "====================================="
echo "Test de CORS headers:"
curl -s -I -H "Origin: http://localhost" http://157.245.164.138:8080/api/market/prices | grep -i "access-control\|cors"

echo ""
echo "4. 💰 Test de endpoints de trading (con autenticación simulada):"
echo "================================================================"
echo "Primero necesitamos un token válido..."

# Intentar login para obtener token
LOGIN_RESPONSE=$(curl -s -X POST \
     -H "Content-Type: application/json" \
     -d '{"email":"angel@gmail.com","password":"admin"}' \
     http://157.245.164.138:8080/api/auth/login)

echo "Respuesta de login: $LOGIN_RESPONSE"

echo ""
echo "5. 📋 Verificar portafolio sin autenticación:"
echo "============================================="
# Test endpoints que requieren auth pero para ver qué error da
curl -v http://157.245.164.138:8080/api/portafolio/resumen 2>&1 | head -10

echo ""
echo "========================================="
echo "🎯 DIAGNÓSTICO:"
echo "========================================="
echo ""
echo "PROBLEMA 1: CORS para requests complejos"
echo "  - El frontend hace requests con headers específicos"
echo "  - Puede necesitar configuración CORS más específica"
echo ""
echo "PROBLEMA 2: Autenticación en endpoints protegidos"
echo "  - Trading requiere JWT token"
echo "  - Portafolio requiere autenticación"
echo ""
echo "PROBLEMA 3: Headers de respuesta"
echo "  - Pueden faltar headers CORS en respuestas"
echo ""
echo "========================================="
echo "🔧 SOLUCIONES:"
echo "========================================="
echo ""
echo "1. Agregar @CrossOrigin a TODOS los controladores"
echo "2. Verificar que el frontend envía headers correctos"
echo "3. Añadir logs de debug para ver requests"
echo "4. Verificar autenticación JWT en frontend"
