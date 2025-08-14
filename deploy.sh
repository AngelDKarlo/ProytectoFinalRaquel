#!/bin/bash
# deploy.sh - Script de deployment para servidor

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuración
APP_NAME="crypto-trading-backend"
DEPLOY_DIR="/opt/crypto-trading"
BACKUP_DIR="/opt/backups/crypto-trading"
DOMAIN="api.tu-dominio.com"  # Cambia esto

print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar que estamos en el servidor correcto
check_environment() {
    print_status "Verificando entorno..."
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker no está instalado"
        echo "Instalar con: curl -fsSL https://get.docker.com | sh"
        exit 1
    fi
    
    # Verificar Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose no está instalado"
        echo "Instalar con: sudo apt-get install docker-compose"
        exit 1
    fi
    
    print_success "Entorno verificado"
}

# Crear estructura de directorios
setup_directories() {
    print_status "Creando directorios..."
    
    sudo mkdir -p $DEPLOY_DIR
    sudo mkdir -p $BACKUP_DIR
    sudo mkdir -p $DEPLOY_DIR/logs
    sudo mkdir -p $DEPLOY_DIR/data
    sudo mkdir -p $DEPLOY_DIR/ssl
    
    # Permisos
    sudo chown -R $USER:docker $DEPLOY_DIR
    
    print_success "Directorios creados"
}

# Backup de la versión anterior
backup_current() {
    print_status "Creando backup de la versión actual..."
    
    if [ -d "$DEPLOY_DIR/app" ]; then
        BACKUP_NAME="backup-$(date +%Y%m%d-%H%M%S).tar.gz"
        sudo tar -czf "$BACKUP_DIR/$BACKUP_NAME" -C "$DEPLOY_DIR" .
        print_success "Backup creado: $BACKUP_NAME"
    else
        print_warning "No hay versión anterior para respaldar"
    fi
}

# Copiar archivos del proyecto
deploy_files() {
    print_status "Copiando archivos del proyecto..."
    
    # Copiar todos los archivos necesarios
    cp -r src pom.xml Dockerfile docker-compose.yml docker-entrypoint.sh nginx.conf *.sql $DEPLOY_DIR/
    
    # Copiar archivo de entorno si no existe
    if [ ! -f "$DEPLOY_DIR/.env" ]; then
        cp .env.production $DEPLOY_DIR/.env
        print_warning "⚠️  Archivo .env creado - EDITA LAS CREDENCIALES EN: $DEPLOY_DIR/.env"
    fi
    
    # Hacer ejecutable el entrypoint
    chmod +x $DEPLOY_DIR/docker-entrypoint.sh
    
    print_success "Archivos copiados"
}

# Construir y levantar contenedores
deploy_containers() {
    print_status "Construyendo y levantando contenedores..."
    
    cd $DEPLOY_DIR
    
    # Detener contenedores antiguos si existen
    docker-compose down || true
    
    # Construir imágenes
    docker-compose build --no-cache
    
    # Levantar contenedores
    docker-compose up -d
    
    # Esperar a que estén listos
    print_status "Esperando a que los servicios estén listos..."
    sleep 30
    
    # Verificar salud
    if docker-compose ps | grep -q "Up"; then
        print_success "Contenedores funcionando"
    else
        print_error "Error al levantar contenedores"
        docker-compose logs
        exit 1
    fi
}

# Configurar firewall
setup_firewall() {
    print_status "Configurando firewall..."
    
    # Abrir puertos necesarios
    sudo ufw allow 80/tcp comment 'HTTP'
    sudo ufw allow 443/tcp comment 'HTTPS'
    sudo ufw allow 8080/tcp comment 'Spring Boot'
    sudo ufw allow 22/tcp comment 'SSH'
    
    # Activar firewall si no está activo
    sudo ufw --force enable
    
    print_success "Firewall configurado"
}

# Configurar SSL con Let's Encrypt
setup_ssl() {
    print_status "Configurando SSL..."
    
    if [ "$1" == "--with-ssl" ]; then
        # Instalar certbot si no está instalado
        if ! command -v certbot &> /dev/null; then
            sudo apt-get update
            sudo apt-get install -y certbot
        fi
        
        # Obtener certificado
        sudo certbot certonly --standalone -d $DOMAIN --non-interactive --agree-tos --email admin@$DOMAIN
        
        # Copiar certificados
        sudo cp /etc/letsencrypt/live/$DOMAIN/fullchain.pem $DEPLOY_DIR/ssl/cert.pem
        sudo cp /etc/letsencrypt/live/$DOMAIN/privkey.pem $DEPLOY_DIR/ssl/key.pem
        
        print_success "SSL configurado para $DOMAIN"
    else
        print_warning "SSL no configurado (usa --with-ssl para activar)"
    fi
}

# Configurar monitoreo
setup_monitoring() {
    print_status "Configurando monitoreo..."
    
    # Crear script de health check
    cat > $DEPLOY_DIR/health-check.sh << 'EOF'
#!/bin/bash
HEALTH_URL="http://localhost:8080/api/market/prices"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)

if [ $HTTP_CODE -eq 200 ]; then
    echo "✅ API is healthy"
    exit 0
else
    echo "❌ API is down (HTTP $HTTP_CODE)"
    # Aquí podrías enviar una notificación
    exit 1
fi
EOF
    
    chmod +x $DEPLOY_DIR/health-check.sh
    
    # Agregar al crontab para chequeo cada 5 minutos
    (crontab -l 2>/dev/null; echo "*/5 * * * * $DEPLOY_DIR/health-check.sh") | crontab -
    
    print_success "Monitoreo configurado"
}

# Configurar logs rotation
setup_log_rotation() {
    print_status "Configurando rotación de logs..."
    
    sudo tee /etc/logrotate.d/crypto-trading > /dev/null << EOF
$DEPLOY_DIR/logs/*.log {
    daily
    rotate 30
    compress
    delaycompress
    notifempty
    create 640 $USER docker
    sharedscripts
    postrotate
        docker-compose -f $DEPLOY_DIR/docker-compose.yml kill -s USR1 app
    endscript
}
EOF
    
    print_success "Rotación de logs configurada"
}

# Mostrar información post-deploy
show_info() {
    echo ""
    echo "========================================="
    echo -e "${GREEN}🎉 Deployment completado exitosamente!${NC}"
    echo "========================================="
    echo ""
    echo "📊 URLs de acceso:"
    echo "   - API: http://$DOMAIN/api"
    echo "   - Health: http://$DOMAIN/health"
    echo ""
    echo "🔧 Comandos útiles:"
    echo "   - Ver logs: docker-compose -f $DEPLOY_DIR/docker-compose.yml logs -f"
    echo "   - Reiniciar: docker-compose -f $DEPLOY_DIR/docker-compose.yml restart"
    echo "   - Detener: docker-compose -f $DEPLOY_DIR/docker-compose.yml down"
    echo "   - Estado: docker-compose -f $DEPLOY_DIR/docker-compose.yml ps"
    echo ""
    echo "📁 Ubicaciones importantes:"
    echo "   - Aplicación: $DEPLOY_DIR"
    echo "   - Logs: $DEPLOY_DIR/logs"
    echo "   - Backups: $BACKUP_DIR"
    echo "   - Configuración: $DEPLOY_DIR/.env"
    echo ""
    echo -e "${YELLOW}⚠️  IMPORTANTE:${NC}"
    echo "   1. Edita $DEPLOY_DIR/.env con tus credenciales reales"
    echo "   2. Configura tu dominio en nginx.conf"
    echo "   3. Considera activar SSL con: $0 --with-ssl"
    echo ""
}

# Función principal
main() {
    echo "🚀 Iniciando deployment de $APP_NAME"
    echo ""
    
    check_environment
    setup_directories
    backup_current
    deploy_files
    deploy_containers
    setup_firewall
    setup_ssl $1
    setup_monitoring
    setup_log_rotation
    
    show_info
}

# Ejecutar
main $@
