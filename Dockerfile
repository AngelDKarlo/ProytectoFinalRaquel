# Dockerfile - Simplificado
FROM eclipse-temurin:17-jdk-alpine as builder

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de Maven primero (para cache de dependencias)
COPY pom.xml .
COPY src ./src

# Instalar Maven y compilar aplicación
RUN apk add --no-cache maven && mvn clean package -DskipTests

# =================================================
# Imagen final de producción
# =================================================
FROM eclipse-temurin:17-jre-alpine

# Directorio de trabajo
WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Ejecutar aplicación
CMD ["java", "-jar", "app.jar"]
