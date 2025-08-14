# Dockerfile para Crypto Trading Simulator
# Build stage
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .

# Descargar dependencias (se cachea si no cambia pom.xml)
RUN mvn dependency:go-offline

# Copiar código fuente
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Crear usuario no root por seguridad
RUN useradd -m -u 1001 appuser && \
    mkdir -p /app/logs /app/data && \
    chown -R appuser:appuser /app

# Copiar JAR desde build stage
COPY --from=build /app/target/cripto-trading-simulator-*.jar app.jar

# Copiar scripts de configuración
COPY --chown=appuser:appuser docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

# Cambiar a usuario no root
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/market/prices || exit 1

# Ejecutar aplicación
ENTRYPOINT ["/app/docker-entrypoint.sh"]
