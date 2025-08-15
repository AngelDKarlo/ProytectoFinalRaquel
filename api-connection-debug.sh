#!/bin/bash
# api-connection-debug.sh - Diagnóstico de conectividad API

echo "🔍 DIAGNÓSTICO DE CONECTIVIDAD API"
echo "=================================="

# 1. Verificar que Spring Boot responde
echo "1. 🧪 Test básico de Spring Boot:"
echo "================================"
curl -v http://157.245.164.138:8080/api/market/prices 2>&1 | head -20

echo ""
echo "2. 🧪 Test de headers CORS:"
echo "==========================="
curl -v -H "Origin: http://localhost" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://157.245.164.138:8080/api/market/prices 2>&1 | grep -i "access-control\|origin\|cors"

echo ""
echo "3. 🧪 Test desde navegador simulado:"
echo "====================================="
curl -v -H "Origin: null" \
     -H "User-Agent: Mozilla/5.0" \
     http://157.245.164.138:8080/api/market/prices 2>&1 | head -10

echo ""
echo "4. 🔍 Verificar configuración CORS en Spring Boot:"
echo "=================================================="
echo "Verificando logs de Spring Boot para errores CORS..."
docker logs crypto-backend --tail=30 | grep -i "cors\|origin\|access-control" || echo "No hay logs CORS específicos"

echo ""
echo "5. 🔧 Test directo de endpoints:"
echo "==============================="
echo "Auth endpoint:"
curl -s -X POST -H "Content-Type: application/json" \
     -d '{"email":"test@test.com","password":"123456"}' \
     http://157.245.164.138:8080/api/auth/login | head -5

echo ""
echo "6. 🌐 Verificar firewall:"
echo "========================"
echo "Puerto 8080:"
ss -tulpn | grep :8080 || echo "Puerto 8080 no visible"

echo ""
echo "========================================="
echo "🎯 PROBLEMAS IDENTIFICADOS:"
echo "========================================="
echo ""
echo "PROBLEMA 1: CORS (Cross-Origin Resource Sharing)"
echo "  - Tu frontend corre en navegador (origen diferente)"
echo "  - Spring Boot necesita permitir CORS explícitamente"
echo ""
echo "PROBLEMA 2: Configuración SecurityConfig"
echo "  - Puede estar bloqueando requests de navegador"
echo ""
echo "PROBLEMA 3: Headers necesarios"
echo "  - Navegadores envían headers específicos"
echo ""
echo "========================================="
echo "🔧 SOLUCIONES:"
echo "========================================="
echo ""
echo "1. Arreglar CORS en Spring Boot"
echo "2. Verificar SecurityConfig"
echo "3. Añadir headers CORS correctos"
echo "4. Probar desde Postman vs navegador"
