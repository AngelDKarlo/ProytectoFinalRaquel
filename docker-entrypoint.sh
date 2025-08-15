#!/bin/sh
echo "🚀 Starting Crypto Trading Simulator Backend..."

# Esperar a que MySQL esté listo
echo "⏳ Waiting for MySQL to be ready..."
sleep 15

# Configurar opciones de JVM
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xmx512m -Xms256m"
fi

JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production}"

echo "📊 JVM Options: $JAVA_OPTS"
echo "🔗 Database URL: $SPRING_DATASOURCE_URL"
echo "👤 Database User: $SPRING_DATASOURCE_USERNAME"
echo "🌐 Server Port: ${SERVER_PORT:-8080}"

# Encontrar el JAR generado
JAR_FILE=$(find /app/target -name "*.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ No se encontró archivo JAR"
    exit 1
fi

echo "🎯 Ejecutando: $JAR_FILE"

# Ejecutar la aplicación
exec java $JAVA_OPTS -jar "$JAR_FILE"
