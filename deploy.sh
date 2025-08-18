#!/bin/bash
# deploy.sh - Script de despliegue automatizado para la API de Crypto Trading

set -e  # Salir si hay algún error

echo "🚀 INICIANDO DESPLIEGUE DE CRYPTO TRADING API"
echo "=============================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Verificar dependencias
check_dependencies() {
    log "Verificando dependencias..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado. Por favor instálalo primero."
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado. Por favor instálalo primero."
    fi
    
    if ! command -v git &> /dev/null; then
        warning "Git no está instalado. Algunas funciones pueden no funcionar."
    fi
    
    log "✅ Todas las dependencias están instaladas"
}

# Verificar archivo .env
check_env_file() {
    log "Verificando archivo de configuración..."
    
    if [ ! -f .env ]; then
        warning "Archivo .env no encontrado. Creando uno por defecto..."
        cat > .env << 'EOF'
# Variables de Entorno para Producción
MYSQL_ROOT_PASSWORD=CryptoTrading2024SecurePassword!
MYSQL_PASSWORD=CryptoUserPass2024!
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/Cripto_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=crypto_user
SPRING_DATASOURCE_PASSWORD=CryptoUserPass2024!

# JWT
JWT_SECRET=SuperSecretKeyForProductionMinimum512BitsLongForHS512Algorithm2024CryptoTrading
JWT_EXPIRATION=86400000

# Servidor
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production

# Trading
TRADING_COMMISSION=0.001
TRADING_INITIAL_BALANCE=10000
EOF
        warning "⚠️  IMPORTANTE: Edita el archivo .env con tus configuraciones de producción"
        warning "⚠️  Especialmente cambia las contraseñas y el JWT_SECRET"
    fi
    
    log "✅ Archivo .env verificado"
}

# Limpiar contenedores anteriores
cleanup_containers() {
    log "Limpiando contenedores anteriores..."
    
    docker-compose down --remove-orphans 2>/dev/null || true
    docker system prune -f 2>/dev/null || true
    
    # Eliminar imágenes antiguas de la aplicación
    docker rmi crypto-trading-api_api 2>/dev/null || true
    docker rmi crypto-trading-api-api 2>/dev/null || true
    
    log "✅ Limpieza completada"
}

# Construir y desplegar
deploy() {
    log "Construyendo y desplegando aplicación..."
    
    # Construir sin caché para asegurar última versión
    log "📦 Construyendo imagen Docker..."
    docker-compose build --no-cache api
    
    # Levantar servicios
    log "🚀 Levantando servicios..."
    docker-compose up -d
    
    log "✅ Servicios desplegados"
}

# Verificar que los servicios estén funcionando
verify_deployment() {
    log "Verificando despliegue..."
    
    # Esperar a que los servicios estén listos
    log "⏳ Esperando que los servicios inicien..."
    sleep 30
    
    # Verificar MySQL
    if docker-compose exec mysql mysqladmin ping -h localhost --silent; then
        log "✅ MySQL está funcionando"
    else
        error "❌ MySQL no está respondiendo"
    fi
    
    # Verificar API
    max_attempts=12
    attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        log "🔍 Verificando API (intento $attempt/$max_attempts)..."
        
        if curl -f -s http://localhost:8080/actuator/health > /dev/null; then
            log "✅ API está funcionando correctamente"
            break
        elif [ $attempt -eq $max_attempts ]; then
            error "❌ API no está respondiendo después de $max_attempts intentos"
        else
            log "⏳ API aún no está lista, esperando 10 segundos..."
            sleep 10
        fi
        
        ((attempt++))
    done
}

# Mostrar información del despliegue
show_deployment_info() {
    log "📋 INFORMACIÓN DEL DESPLIEGUE"
    echo "=============================="
    
    info "🌐 API URL: http://localhost:8080"
    info "📊 Health Check: http://localhost:8080/actuator/health"
    info "🔧 Debug Endpoints: http://localhost:8080/api/debug/"
    info "🔐 Auth Endpoints: http://localhost:8080/api/auth/"
    info "📈 Market Endpoints: http://localhost:8080/api/market/"
    
    echo ""
    info "📝 Logs de la aplicación:"
    echo "   docker-compose logs -f api"
    
    echo ""
    info "🔍 Estado de los servicios:"
    docker-compose ps
    
    echo ""
    info "🧪 Test rápido de la API:"
    curl -s http://localhost:8080/api/market/test | head -3
}

# Función principal
main() {
    log "🎯 Iniciando proceso de despliegue..."
    
    check_dependencies
    check_env_file
    cleanup_containers
    deploy
    verify_deployment
    show_deployment_info
    
    echo ""
    log "🎉 ¡DESPLIEGUE COMPLETADO EXITOSAMENTE!"
    echo "=============================================="
    warning "📝 Recuerda:"
    warning "   1. Configurar tu dominio si vas a usar HTTPS"
    warning "   2. Configurar certificados SSL para producción"
    warning "   3. Revisar y ajustar las configuraciones en .env"
    warning "   4. Hacer backup de la base de datos regularmente"
}

# Manejar interrupciones
trap 'error "❌ Despliegue interrumpido"' INT TERM

# Ejecutar si es llamado directamente
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
