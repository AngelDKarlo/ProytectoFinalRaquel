# Dockerfile - Multi-stage optimizado
FROM eclipse-temurin:17-jdk-alpine as builder

# Instalar Maven
RUN apk add --no-cache maven

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven primero (para cache de dependencias)
COPY pom.xml .
COPY src ./src

# Compilar aplicación
RUN mvn clean package -DskipTests

# =================================================
# Imagen final de producción
# =================================================
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

# Instalar dependencias necesarias
RUN apk add --no-cache curl

# Crear directorios
RUN mkdir -p /app/logs && \
    mkdir -p /app/data && \
    chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring

# Directorio de trabajo
WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /app/target/*.jar app.jar

# Variables de entorno por defecto
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=production

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
