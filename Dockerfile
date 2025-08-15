FROM openjdk:17-jdk-slim
WORKDIR /app

# Instalar Maven, curl y dependencias
RUN apt-get update && \
    apt-get install -y maven curl wget && \
    rm -rf /var/lib/apt/lists/*

# Copiar archivos del proyecto
COPY pom.xml .
COPY src ./src
COPY docker-entrypoint.sh .

# Compilar la aplicación
RUN mvn clean package -DskipTests

# Crear usuario no root
RUN useradd -m -u 1001 appuser && \
    mkdir -p /app/logs /app/data && \
    chown -R appuser:appuser /app && \
    chmod +x /app/docker-entrypoint.sh

# Cambiar a usuario no root
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Ejecutar aplicación
ENTRYPOINT ["/app/docker-entrypoint.sh"]
