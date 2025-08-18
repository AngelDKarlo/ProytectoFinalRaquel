#!/bin/bash
# install.sh - Instalación completa del servidor de Crypto Trading API

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() { echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"; }
error() { echo -e "${RED}[ERROR] $1${NC}"; exit 1; }
warning() { echo -e "${YELLOW}[WARNING] $1${NC}"; }
info() { echo -e "${BLUE}[INFO] $1${NC}"; }

echo "🚀 INSTALACIÓN COMPLETA - CRYPTO TRADING API"
echo "=============================================="

# 1. Detectar sistema operativo
detect_os() {
    log "Detectando sistema operativo..."
    
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        if [ -f /etc/debian_version ]; then
            OS="debian"
            log "✅ Sistema detectado: Debian/Ubuntu"
        elif [ -f /etc/redhat-release ]; then
            OS="redhat"
            log "✅ Sistema detectado: RedHat/CentOS"
        else
            OS="linux"
            log "✅ Sistema detectado: Linux genérico"
        fi
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
        log "✅ Sistema detectado: macOS"
    else
        error "❌ Sistema operativo no soportado: $OSTYPE"
    fi
}

# 2. Instalar Docker
install_docker() {
    log "Instalando Docker..."
    
    if command -v docker &> /dev/null; then
        log "✅ Docker ya está instalado"
        return
    fi
    
    case $OS in
        "debian")
            sudo apt-get update
            sudo apt-get install -y ca-certificates curl gnupg
            sudo install -m 0755 -d /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            sudo chmod a+r /etc/apt/keyrings/docker.gpg
            
            echo \
              "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
              "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
              sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
            
            sudo apt-get update
            sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
            ;;
        "redhat")
            sudo yum install -y yum-utils
            sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
            sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
            sudo systemctl start docker
            sudo systemctl enable docker
            ;;
        "macos")
            if command -v brew &> /dev/null; then
                brew install --cask docker
            else
                error "❌ Por favor instala Docker Desktop desde https://www.docker.com/products/docker-desktop"
            fi
            ;;
        *)
            error "❌ Instalación automática de Docker no disponible para este OS"
            ;;
    esac
    
    # Agregar usuario al grupo docker (Linux)
    if [[ "$OS" != "macos" ]]; then
        sudo usermod -aG docker $USER
        warning "⚠️  Debes cerrar sesión y volver a entrar para usar Docker sin sudo"
    fi
    
    log "✅ Docker instalado correctamente"
}

# 3. Instalar Docker Compose
install_docker_compose() {
    log "Instalando Docker Compose..."
    
    if command -v docker-compose &> /dev/null; then
        log "✅ Docker Compose ya está instalado"
        return
    fi
    
    if docker compose version &> /dev/null; then
        log "✅ Docker Compose (plugin) ya está disponible"
        # Crear alias para compatibilidad
        echo 'alias docker-compose="docker compose"' >> ~/.bashrc
        return
    fi
    
    # Instalar Docker Compose standalone
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    
    log "✅ Docker Compose instalado correctamente"
}

# 4. Instalar herramientas adicionales
install_tools() {
    log "Instalando herramientas adicionales..."
    
    case $OS in
        "debian")
            sudo apt-get update
            sudo apt-get install -y curl wget git nano htop net-tools
            ;;
        "redhat")
            sudo yum install -y curl wget git nano htop net-tools
            ;;
        "macos")
            if command -v brew &> /dev/null; then
                brew install curl wget git nano htop
            fi
            ;;
    esac
    
    log "✅ Herramientas adicionales instaladas"
}

# 5. Configurar firewall
configure_firewall() {
    log "Configurando firewall..."
    
    if [[ "$OS" == "debian" ]]; then
        if command -v ufw &> /dev/null; then
            sudo ufw allow 22/tcp    # SSH
            sudo ufw allow 80/tcp    # HTTP
            sudo ufw allow 443/tcp   # HTTPS
            sudo ufw allow 8080/tcp  # API
            info "Firewall configurado con UFW"
        fi
    elif [[ "$OS" == "redhat" ]]; then
        if command -v firewall-cmd &> /dev/null; then
            sudo firewall-cmd --permanent --add-port=22/tcp
            sudo firewall-cmd --permanent --add-port=80/tcp
            sudo firewall-cmd --permanent --add-port=443/tcp
            sudo firewall-cmd --permanent --add-port=8080/tcp
            sudo firewall-cmd --reload
            info "Firewall configurado con firewalld"
        fi
    fi
    
    log "✅ Firewall configurado"
}

# 6. Crear estructura de directorios
create_project_structure() {
    log "Creando estructura del proyecto..."
    
    PROJECT_DIR="$HOME/crypto-trading-api"
    
    mkdir -p "$PROJECT_DIR"
    cd "$PROJECT_DIR"
    
    mkdir -p {src/main/java/com/trading/cripto,src/main/resources,docker,scripts,logs,ssl}
    
    log "✅ Estructura de proyecto creada en: $PROJECT_DIR"
    export PROJECT_DIR
}

# 7. Crear archivos de configuración
create_config_files() {
    log "Creando archivos de configuración..."
    
    # Crear .env
    cat > .env << 'EOF'
# Variables de Entorno para Producción
MYSQL_ROOT_PASSWORD=CryptoTrading2024SecurePassword!
MYSQL_PASSWORD=CryptoUserPass2024!
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/Cripto_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=crypto_user
SPRING_DATASOURCE_PASSWORD=CryptoUserPass2024!

# JWT - CAMBIAR EN PRODUCCIÓN
JWT_SECRET=SuperSecretKeyForProductionMinimum512BitsLongForHS512Algorithm2024CryptoTrading
JWT_EXPIRATION=86400000

# Servidor
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=production

# Trading
TRADING_COMMISSION=0.001
TRADING_INITIAL_BALANCE=10000
EOF

    # Crear docker-compose.yml básico
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: crypto-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: Cripto_db
      MYSQL_USER: ${SPRING_DATASOURCE_USERNAME}
      MYSQL_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - crypto-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10

  api:
    build: .
    container_name: crypto-api
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      SERVER_PORT: ${SERVER_PORT}
      SPRING_PROFILES_ACTIVE: production
    ports:
      - "8080:8080"
    volumes:
      - api_logs:/app/logs
    networks:
      - crypto-network

networks:
  crypto-network:
    driver: bridge

volumes:
  mysql_data:
  api_logs:
EOF

    # Crear script de monitoreo
    cat > scripts/monitor.sh << 'EOF'
#!/bin/bash
# monitor.sh - Monitoreo del estado de la API

echo "📊 ESTADO DE CRYPTO TRADING API"
echo "================================"

echo "🐳 Estado de contenedores:"
docker-compose ps

echo ""
echo "🔍 Health Checks:"
echo "MySQL:"
docker-compose exec mysql mysqladmin ping -h localhost --silent && echo "✅ OK" || echo "❌ ERROR"

echo "API:"
curl -f -s http://localhost:8080/actuator/health > /dev/null && echo "✅ OK" || echo "❌ ERROR"

echo ""
echo "📈 Uso de recursos:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"

echo ""
echo "📝 Últimos logs de la API:"
docker-compose logs --tail=10 api
EOF

    chmod +x scripts/monitor.sh
    
    log "✅ Archivos de configuración creados"
}

# 8. Verificar instalación
verify_installation() {
    log "Verificando instalación..."
    
    # Verificar Docker
    if docker --version &> /dev/null; then
        log "✅ Docker: $(docker --version)"
    else
        error "❌ Docker no funciona correctamente"
    fi
    
    # Verificar Docker Compose
    if docker-compose --version &> /dev/null || docker compose version &> /dev/null; then
        log "✅ Docker Compose disponible"
    else
        error "❌ Docker Compose no funciona correctamente"
    fi
    
    log "✅ Instalación verificada correctamente"
}

# 9. Mostrar instrucciones finales
show_final_instructions() {
    log "📋 INSTALACIÓN COMPLETADA"
    echo "=========================="
    
    info "📁 Proyecto creado en: $PROJECT_DIR"
    info "🔧 Archivos de configuración listos"
    
    echo ""
    warning "⚠️  PASOS SIGUIENTES:"
    echo "1. cd $PROJECT_DIR"
    echo "2. Editar .env con tus configuraciones"
    echo "3. Agregar tu código fuente a src/"
    echo "4. Crear Dockerfile"
    echo "5. Ejecutar: docker-compose up -d"
    
    echo ""
    info "🚀 Comandos útiles:"
    echo "   Monitorear: ./scripts/monitor.sh"
    echo "   Ver logs: docker-compose logs -f"
    echo "   Parar: docker-compose down"
    echo "   Reiniciar: docker-compose restart"
    
    echo ""
    warning "🔒 SEGURIDAD:"
    echo "   - Cambia las contraseñas en .env"
    echo "   - Configura JWT_SECRET único"
    echo "   - Configura firewall para producción"
    echo "   - Considera usar HTTPS con certificados SSL"
}

# Función principal
main() {
    log "🎯 Iniciando instalación completa..."
    
    detect_os
    install_docker
    install_docker_compose
    install_tools
    configure_firewall
    create_project_structure
    create_config_files
    verify_installation
    show_final_instructions
    
    echo ""
    log "🎉 ¡INSTALACIÓN COMPLETADA EXITOSAMENTE!"
}

# Ejecutar
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
