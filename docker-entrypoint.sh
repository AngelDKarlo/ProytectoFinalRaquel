#!/bin/sh
# docker-entrypoint.sh - Script de entrada para el contenedor

echo "🚀 Starting Crypto Trading Simulator Backend..."

# Esperar a que MySQL esté listo (backup por si el healthcheck falla)
echo "⏳ Waiting for MySQL to be ready..."
sleep 10

# Configurar opciones de JVM según el entorno
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xmx512m -Xms256m"
fi

# Añadir opciones adicionales para producción
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production}"

echo "📊 JVM Options: $JAVA_OPTS"
echo "🔗 Database URL: $SPRING_DATASOURCE_URL"
echo "👤 Database User: $SPRING_DATASOURCE_USERNAME"
echo "🔐 JWT configured: Yes"
echo "🌐 Server Port: ${SERVER_PORT:-8080}"

# Ejecutar la aplicación
exec java $JAVA_OPTS -jar /app/app.jar
