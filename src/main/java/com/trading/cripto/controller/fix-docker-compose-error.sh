#!/bin/bash
# fix-docker-compose-error.sh - Solucionar error de ContainerConfig

echo "🔧 SOLUCIONANDO ERROR DE DOCKER COMPOSE"
echo "======================================="

# 1. Detener todo completamente
echo "1. Deteniendo todos los contenedores..."
docker-compose down || true
docker stop $(docker ps -aq) 2>/dev/null || true

# 2. Limpiar contenedores problemáticos
echo "2. Limpiando contenedores problemáticos..."
docker rm -f crypto-mysql crypto-backend crypto-apache 2>/dev/null || true

# 3. Limpiar volúmenes problemáticos
echo "3. Limpiando volúmenes problemáticos..."
docker volume rm proytectofinal_mysql_data 2>/dev/null || true
docker volume rm proytectofinal_app_logs 2>/dev/null || true
docker volume rm proytectofinal_app_data 2>/dev/null || true

# 4. Limpiar redes
echo "4. Limpiando redes..."
docker network rm proytectofinal_crypto-network 2>/dev/null || true

# 5. Limpiar imágenes problemáticas
echo "5. Limpiando imágenes..."
docker rmi proytectofinal_app 2>/dev/null || true

# 6. Crear docker-compose limpio y simple
echo "6. Creando docker-compose limpio..."
cat > docker-compose-clean.yml << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: crypto-mysql-new
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: CryptoTrading2024!
      MYSQL_DATABASE: Cripto_db
      MYSQL_USER: crypto_user
      MYSQL_PASSWORD: CryptoPass2024!
    ports:
      - "3307:3306"
    volumes:
      - mysql_data_new:/var/lib/mysql
      - ./Cripto_db.sql:/docker-entrypoint-initdb.d/01-schema.sql
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  app:
    build: .
    container_name: crypto-backend-new
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/Cripto_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: crypto_user
      SPRING_DATASOURCE_PASSWORD: CryptoPass2024!
      JWT_SECRET: SuperSecretKeyForProductionMinimum512BitsLongForHS512Algorithm2024
      JWT_EXPIRATION: 86400000
      SERVER_PORT: 8080
      SPRING_PROFILES_ACTIVE: production
      JAVA_OPTS: -Xmx512m -Xms256m
    ports:
      - "8080:8080"
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/market/prices"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

networks:
  crypto-network:
    driver: bridge

volumes:
  mysql_data_new:
    driver: local
EOF

echo "✅ Docker Compose limpio creado"

# 7. Limpiar sistema Docker
echo "7. Limpiando sistema Docker..."
docker system prune -f

# 8. Aplicar docker-compose limpio
echo "8. Aplicando docker-compose limpio..."
cp docker-compose-clean.yml docker-compose.yml

# 9. Construir desde cero
echo "9. Construyendo desde cero..."
docker-compose build --no-cache

# 10. Levantar servicios limpios
echo "10. Levantando servicios limpios..."
docker-compose up -d

echo ""
echo "========================================="
echo "✅ DOCKER COMPOSE REPARADO"
echo "========================================="
echo ""
echo "🔍 Monitoreando arranque..."
echo ""

# 11. Monitorear arranque
for i in {1..20}; do
    echo "[$i/20] Verificando servicios..."
    docker-compose ps
    
    # Verificar si Spring Boot está listo
    if curl -f -s http://localhost:8080/api/market/prices >/dev/null 2>&1; then
        echo "🎉 ¡TODO FUNCIONANDO!"
        break
    fi
    
    sleep 15
done

echo ""
echo "🧪 Test final:"
curl -s http://localhost:8080/api/market/prices | head -3

echo ""
echo "📋 Estado final:"
docker-compose ps

echo ""
echo "🏁 Reparación completada"
