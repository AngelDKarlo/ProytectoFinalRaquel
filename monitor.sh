#!/bin/bash
echo "🔍 Estado de Crypto Trading Simulator"
echo "====================================="
echo ""
echo "📊 Contenedores:"
docker-compose ps
echo ""
echo "🌐 Test de API:"
if curl -f http://localhost/api/market/prices &> /dev/null; then
    echo "✅ API funcionando correctamente"
else
    echo "❌ API no responde"
fi
echo ""
echo "💾 Uso de espacio:"
df -h | grep -E "/$|/var"
echo ""
echo "🧠 Uso de memoria:"
free -h
