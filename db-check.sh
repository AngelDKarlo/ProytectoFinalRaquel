#!/bin/bash
# db-check.sh - Script para verificar la conexión a la base de datos

echo "🔍 Verificando conexión a la base de datos..."

# Verificar si MySQL está corriendo
if ! mysqladmin ping -h"localhost" --silent 2>/dev/null; then
    echo "❌ MySQL no está corriendo"
    echo "💡 Inicia con: sudo systemctl start mysql"
    exit 1
fi

echo "✅ MySQL está corriendo"

# Verificar base de datos
DB_NAME="Cripto_db"
if mysql -e "USE $DB_NAME;" 2>/dev/null; then
    echo "✅ Base de datos '$DB_NAME' existe"
    
    # Mostrar tablas
    echo ""
    echo "📋 Tablas en la base de datos:"
    mysql -D $DB_NAME -e "SHOW TABLES;" 2>/dev/null | tail -n +2 | sed 's/^/   /'
    
    # Verificar datos en tablas importantes
    echo ""
    echo "📊 Conteo de registros:"
    
    echo -n "   users: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM users;" 2>/dev/null | tail -n 1
    
    echo -n "   cripto: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM cripto;" 2>/dev/null | tail -n 1
    
    echo -n "   price_history: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM price_history;" 2>/dev/null | tail -n 1
    
    echo -n "   portafolio_usuario: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM portafolio_usuario;" 2>/dev/null | tail -n 1
    
    echo -n "   wallets: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM wallets;" 2>/dev/null | tail -n 1
    
    echo -n "   transacciones_ejecutadas: "
    mysql -D $DB_NAME -e "SELECT COUNT(*) FROM transacciones_ejecutadas;" 2>/dev/null | tail -n 1
    
    # Mostrar últimos registros de price_history
    echo ""
    echo "📈 Últimos 5 registros de price_history:"
    mysql -D $DB_NAME -e "SELECT crypto_id, precio, timestamp FROM price_history ORDER BY timestamp DESC LIMIT 5;" 2>/dev/null
    
    # Mostrar precios actuales de cripto
    echo ""
    echo "💰 Precios actuales de criptomonedas:"
    mysql -D $DB_NAME -e "SELECT simbolo, nombre, precio FROM cripto;" 2>/dev/null
    
else
    echo "❌ Base de datos '$DB_NAME' no existe"
    exit 1
fi

echo ""
echo "🔧 Para conectarte manualmente a MySQL:"
echo "   mysql -u root -p"
echo "   USE Cripto_db;"
echo "   SHOW TABLES;"
