#!/bin/bash
# docker-entrypoint.sh - Script de entrada para el contenedor

echo "🚀 Starting Crypto Trading Simulator API..."

# Esperar a que MySQL esté listo
echo "⏳ Waiting for MySQL to be ready..."
until curl -f -s http://mysql:3306 > /dev/null 2>&1 || [ $? -eq 52 ]; do
    echo "🔄 MySQL not ready yet, waiting 5 seconds..."
    sleep 5
done

echo "✅ MySQL is ready!"

# Configurar opciones de JVM según el entorno
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xmx512m -Xms256m"
fi

# Añadir opciones adicionales para producción
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-production}"
JAVA_OPTS="$JAVA_OPTS -Dfile.encoding=UTF-8"
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=UTC"

echo "📊 Environment Configuration:"
echo "   JVM Options: $JAVA_OPTS"
echo "   Database URL: $SPRING_DATASOURCE_URL"
echo "   Database User: $SPRING_DATASOURCE_USERNAME"
echo "   Server Port: ${SERVER_PORT:-8080}"
echo "   Active Profile: ${SPRING_PROFILES_ACTIVE:-production}"

# Crear directorio de logs si no existe
mkdir -p /app/logs

# Ejecutar la aplicación
echo "🎯 Starting Spring Boot Application..."
exec java $JAVA_OPTS -jar /app/app.jar
