#!/bin/bash
# deploy-quick.sh - Script rápido para deploy del Trading Crypto Simulator

set -e

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
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

echo "🚀 Deploy Rápido - Crypto Trading Simulator"
echo "=============================================="

# 1. Verificar prerequisitos
print_status "Verificando prerequisitos..."

if ! command -v docker &> /dev/null; then
    print_error "Docker no está instalado"
    echo "Instalar con: curl -fsSL https://get.docker.com | sh"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose no está instalado"
    echo "Instalar con: sudo apt-get install docker-compose-plugin"
    exit 1
fi

print_success "✅ Docker y Docker Compose encontrados"

# 2. Crear .env si no existe
if [ ! -f ".env" ]; then
    print_warning "⚠️ Archivo .env no encontrado, creando desde .env.production..."
    cp .env.production .env
    print_warning "⚠️ IMPORTANTE: Edita .env con tus contraseñas reales antes de continuar"
    echo "Presiona Enter cuando hayas editado .env o Ctrl+C para cancelar"
    read
fi

# 3. Verificar archivos necesarios
print_status "Verificando archivos necesarios..."

required_files=("docker-compose.yml" "Dockerfile" "apache.conf" "Cripto_db.sql" "pom.xml")
for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        print_error "❌ Archivo requerido no encontrado: $file"
        exit 1
    fi
done

print_success "✅ Todos los archivos necesarios encontrados"

# 4. Crear directorios necesarios
print_status "Creando directorios..."
mkdir -p ssl logs data

# 5. Detener servicios anteriores si existen
print_status "Deteniendo servicios anteriores..."
docker-compose down || true

# 6. Construir imágenes
print_status "Construyendo imágenes Docker..."
docker-compose build --no-cache

# 7. Levantar servicios
print_status "Levantando servicios..."
docker-compose up -d

# 8. Esperar a que estén listos
print_status "Esperando a que los servicios estén listos..."
sleep 30

# 9. Verificar estado
print_status "Verificando estado de los servicios..."

if docker-compose ps | grep -q "Up"; then
    print_success "✅ Contenedores funcionando"
else
    print_error "❌ Error al levantar contenedores"
    echo "Logs de errores:"
    docker-compose logs
    exit 1
fi

# 10. Test de conectividad
print_status "Probando conectividad de la API..."
sleep 10  # Dar tiempo extra para que Spring Boot arranque

if curl -f http://localhost/api/market/prices &> /dev/null; then
    print_success "✅ API respondiendo correctamente"
elif curl -f http://localhost:8080/api/market/prices &> /dev/null; then
    print_warning "⚠️ API responde en puerto 8080 pero no a través de Apache"
    echo "Verifica la configuración de Apache"
else
    print_error "❌ API no responde"
    echo "Logs de la aplicación:"
    docker-compose logs app | tail -20
fi

# 11. Configurar firewall básico
print_status "Configurando firewall básico..."
if command -v ufw &> /dev/null; then
    sudo ufw allow 22/tcp comment 'SSH' || true
    sudo ufw allow 80/tcp comment 'HTTP' || true
    sudo ufw allow 443/tcp comment 'HTTPS' || true
    sudo ufw --force enable || true
    print_success "✅ Firewall configurado"
else
    print_warning "⚠️ UFW no encontrado, configurar firewall manualmente"
fi

# 12. Mostrar información final
echo ""
echo "========================================="
echo -e "${GREEN}🎉 Deploy completado!${NC}"
echo "========================================="
echo ""
echo "📊 URLs de acceso:"
echo "   🔗 API: http://$(hostname -I | awk '{print $1}')/api"
echo "   🏥 Health: http://$(hostname -I | awk '{print $1}')/api/market/prices"
echo ""
echo "🔧 Comandos útiles:"
echo "   📋 Ver estado: docker-compose ps"
echo "   📝 Ver logs: docker-compose logs -f"
echo "   🔄 Reiniciar: docker-compose restart"
echo "   🛑 Detener: docker-compose down"
echo ""
echo "📁 Archivos importantes:"
echo "   🔑 Configuración: .env"
echo "   📊 Logs: logs/"
echo "   🗄️ Base de datos: mysql_data/"
echo ""
echo -e "${YELLOW}⚠️ IMPORTANTE:${NC}"
echo "   1. Asegúrate de que .env tiene contraseñas seguras"
echo "   2. Considera configurar SSL/HTTPS para producción"
echo "   3. Configura backup automático de la base de datos"
echo "   4. Monitorea los logs regularmente"
echo ""

# 13. Script de monitoreo rápido
cat > monitor.sh << 'EOF'
#!/bin/bash
echo "🔍 Estado de Crypto Trading Simulator"
echo "====================================="
echo ""
echo "📊 Contenedores:"
docker-compose ps
echo ""
echo "🌐 Test de API:"
if curl -f http://localhost/api/market/prices &> /dev/null; then
    echo "✅ API funcionando correctamente"
else
    echo "❌ API no responde"
fi
echo ""
echo "💾 Uso de espacio:"
df -h | grep -E "/$|/var"
echo ""
echo "🧠 Uso de memoria:"
free -h
EOF

chmod +x monitor.sh
print_success "✅ Script de monitoreo creado: ./monitor.sh"

echo ""
print_success "🚀 Deploy completo! Tu simulador de trading está funcionando."