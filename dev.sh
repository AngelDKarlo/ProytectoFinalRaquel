#!/bin/bash

# dev.sh - Script completo para desarrollo del Trading Crypto Backend

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Configuración
APP_NAME="Trading Crypto Backend"
DB_NAME="Cripto_db"
PORT="8080"
LOG_FILE="logs/dev.log"

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${PURPLE}===============================================${NC}"
    echo -e "${PURPLE}🚀 $1${NC}"
    echo -e "${PURPLE}===============================================${NC}"
}

# Banner de bienvenida
show_banner() {
    clear
    echo -e "${CYAN}"
    echo "████████ ██████   █████  ██████  ██ ███    ██  ██████ "
    echo "   ██    ██   ██ ██   ██ ██   ██ ██ ████   ██ ██      "
    echo "   ██    ██████  ███████ ██   ██ ██ ██ ██  ██ ██   ███"
    echo "   ██    ██   ██ ██   ██ ██   ██ ██ ██  ██ ██ ██    ██"
    echo "   ██    ██   ██ ██   ██ ██████  ██ ██   ████  ██████ "
    echo ""
    echo "    ██████ ██████  ██    ██ ██████  ████████  ██████  "
    echo "   ██      ██   ██  ██  ██  ██   ██    ██    ██    ██ "
    echo "   ██      ██████    ████   ██████     ██    ██    ██ "
    echo "   ██      ██   ██    ██    ██         ██    ██    ██ "
    echo "    ██████ ██   ██    ██    ██         ██     ██████  "
    echo -e "${NC}"
    echo -e "${GREEN}        Simulador de Trading de Criptomonedas${NC}"
    echo -e "${BLUE}              Backend Development Kit${NC}"
    echo ""
}

# Verificar prerequisitos
check_prerequisites() {
    print_header "Verificando Prerequisitos"
    local errors=0

    # Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1-2)
        print_success "✅ Java: $JAVA_VERSION"
    else
        print_error "❌ Java no está instalado"
        echo "   💡 Instala: sudo apt install openjdk-17-jdk"
        errors=$((errors + 1))
    fi

    # Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | awk '{print $3}')
        MVN_CMD="mvn"
        print_success "✅ Maven: $MVN_VERSION"
    else
        if [ -f "./mvnw" ]; then
            MVN_CMD="./mvnw"
            print_warning "⚠️  Maven no encontrado, usando Maven Wrapper"
        else
            print_error "❌ Maven no está instalado"
            echo "   💡 Instala: sudo apt install maven"
            errors=$((errors + 1))
        fi
    fi

    # MySQL
    if command -v mysql &> /dev/null; then
        MYSQL_VERSION=$(mysql --version | awk '{print $3}' | cut -d',' -f1)
        print_success "✅ MySQL: $MYSQL_VERSION"

        # Verificar conexión
        if mysqladmin ping -h"localhost" --silent 2>/dev/null; then
            print_success "✅ MySQL está corriendo"
        else
            print_warning "⚠️  MySQL no está corriendo"
            echo "   💡 Inicia: sudo systemctl start mysql"
        fi
    else
        print_error "❌ MySQL no está instalado"
        echo "   💡 Instala: sudo apt install mysql-server"
        errors=$((errors + 1))
    fi

    # Git (opcional)
    if command -v git &> /dev/null; then
        GIT_VERSION=$(git --version | awk '{print $3}')
        print_success "✅ Git: $GIT_VERSION"
    else
        print_warning "⚠️  Git no está instalado (opcional)"
    fi

    # Curl
    if command -v curl &> /dev/null; then
        print_success "✅ Curl disponible"
    else
        print_warning "⚠️  Curl no está instalado"
        echo "   💡 Instala: sudo apt install curl"
    fi

    if [ $errors -gt 0 ]; then
        print_error "❌ $errors prerequisito(s) faltante(s)"
        exit 1
    fi

    print_success "🎉 Todos los prerequisitos están listos"
}

# Setup inicial del proyecto
setup_project() {
    print_header "Configuración Inicial del Proyecto"

    # Crear directorios necesarios
    print_status "Creando estructura de directorios..."
    mkdir -p logs
    mkdir -p src/main/resources/historical_data
    mkdir -p target

    # Verificar archivos importantes
    if [ ! -f "pom.xml" ]; then
        print_error "❌ pom.xml no encontrado. ¿Estás en el directorio correcto?"
        exit 1
    fi

    if [ ! -f "Cripto_db.sql" ]; then
        print_warning "⚠️  Cripto_db.sql no encontrado"
    fi

    # Crear .gitignore si no existe
    if [ ! -f ".gitignore" ]; then
        print_status "Creando .gitignore..."
        cat > .gitignore << EOF
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db

# IDE
.idea/
*.iml
.vscode/
.eclipse/

# Application
server.pid
application-local.properties
EOF
        print_success "✅ .gitignore creado"
    fi

    print_success "🎉 Estructura del proyecto configurada"
}

# Setup de base de datos
setup_database() {
    print_header "Configuración de Base de Datos"

    # Verificar conexión MySQL
    if ! mysqladmin ping -h"localhost" --silent 2>/dev/null; then
        print_error "❌ No se puede conectar a MySQL"
        print_status "Intentando iniciar MySQL..."
        sudo systemctl start mysql
        sleep 3

        if ! mysqladmin ping -h"localhost" --silent 2>/dev/null; then
            print_error "❌ No se pudo iniciar MySQL"
            exit 1
        fi
    fi

    # Verificar si la BD existe
    if mysql -e "USE $DB_NAME;" 2>/dev/null; then
        print_success "✅ Base de datos '$DB_NAME' ya existe"

        # Mostrar tablas
        print_status "Verificando tablas..."
        TABLES=$(mysql -D $DB_NAME -e "SHOW TABLES;" 2>/dev/null | tail -n +2 | wc -l)
        if [ $TABLES -gt 0 ]; then
            print_success "✅ $TABLES tabla(s) encontrada(s)"
            mysql -D $DB_NAME -e "SHOW TABLES;" 2>/dev/null | tail -n +2 | sed 's/^/   📋 /'
        else
            print_warning "⚠️  Base de datos existe pero está vacía"
        fi
    else
        if [ -f "Cripto_db.sql" ]; then
            print_status "Creando base de datos desde Cripto_db.sql..."
            mysql < Cripto_db.sql
            if [ $? -eq 0 ]; then
                print_success "✅ Base de datos creada exitosamente"
            else
                print_error "❌ Error al crear la base de datos"
                exit 1
            fi
        else
            print_error "❌ Archivo Cripto_db.sql no encontrado"
            exit 1
        fi
    fi
}

# Compilar proyecto
compile_project() {
    print_header "Compilando Proyecto"

    print_status "Limpiando proyecto anterior..."
    $MVN_CMD clean -q

    print_status "Descargando dependencias..."
    $MVN_CMD dependency:resolve -q

    print_status "Compilando código fuente..."
    $MVN_CMD compile -q

    if [ $? -eq 0 ]; then
        print_success "✅ Compilación exitosa"
    else
        print_error "❌ Error en la compilación"
        exit 1
    fi
}

# Ejecutar tests
run_tests() {
    print_header "Ejecutando Tests"

    print_status "Ejecutando tests unitarios..."
    $MVN_CMD test

    if [ $? -eq 0 ]; then
        print_success "✅ Todos los tests pasaron"
    else
        print_warning "⚠️  Algunos tests fallaron"
    fi
}

# Iniciar servidor en modo desarrollo
start_server() {
    print_header "Iniciando Servidor de Desarrollo"

    # Verificar que el puerto esté libre
    if lsof -i :$PORT >/dev/null 2>&1; then
        print_error "❌ Puerto $PORT ya está en uso"
        print_status "Procesos usando el puerto:"
        lsof -i :$PORT
        exit 1
    fi

    # Crear directorio de logs
    mkdir -p logs

    print_success "🚀 Iniciando $APP_NAME..."
    echo ""
    echo "📡 Servidor: http://localhost:$PORT"
    echo "📊 APIs principales:"
    echo "   🏪 GET  /api/market/prices          - Ver precios de criptomonedas"
    echo "   👤 POST /api/auth/register          - Registrar nuevo usuario"
    echo "   💱 POST /api/trading/execute/{id}   - Ejecutar operación de trading"
    echo "   💰 GET  /api/portafolio/resumen/{id} - Ver resumen del portafolio"
    echo "   📈 GET  /api/trading/history/{id}   - Ver historial de transacciones"
    echo ""
    echo "💡 Datos de prueba:"
    echo "   🪙 Criptomonedas: ZOR, NEB, LUM"
    echo "   💵 Saldo inicial: $10,000 USD"
    echo "   📊 Precios se actualizan cada 5 segundos"
    echo "   🔄 Comisión por operación: 0.1%"
    echo ""
    echo "📋 Logs: tail -f $LOG_FILE"
    echo "🛑 Para detener: Ctrl+C"
    echo ""
    print_status "Iniciando Spring Boot..."

    # Ejecutar servidor
    $MVN_CMD spring-boot:run 2>&1 | tee $LOG_FILE
}

# Ver logs en tiempo real
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        print_status "Mostrando logs en tiempo real (Ctrl+C para salir):"
        tail -f "$LOG_FILE"
    else
        print_error "❌ Archivo de log no encontrado: $LOG_FILE"
    fi
}

# Ver estado del proyecto
show_status() {
    print_header "Estado del Proyecto"

    # Info del proyecto
    print_status "📁 Directorio: $(pwd)"
    print_status "🔧 Maven: $MVN_CMD"

    # Estado de archivos importantes
    echo ""
    echo "📋 Archivos del proyecto:"
    [ -f "pom.xml" ] && echo "   ✅ pom.xml" || echo "   ❌ pom.xml"
    [ -f "Cripto_db.sql" ] && echo "   ✅ Cripto_db.sql" || echo "   ❌ Cripto_db.sql"
    [ -d "src" ] && echo "   ✅ src/" || echo "   ❌ src/"
    [ -d "target" ] && echo "   ✅ target/" || echo "   ❌ target/"
    [ -d "logs" ] && echo "   ✅ logs/" || echo "   ❌ logs/"

    # Estado de servicios
    echo ""
    echo "🔧 Servicios:"
    if mysqladmin ping -h"localhost" --silent 2>/dev/null; then
        echo "   ✅ MySQL corriendo"
    else
        echo "   ❌ MySQL detenido"
    fi

    if lsof -i :$PORT >/dev/null 2>&1; then
        echo "   ✅ Servidor corriendo en puerto $PORT"
        echo "   🌐 http://localhost:$PORT"
    else
        echo "   ❌ Servidor no está corriendo"
    fi

    # Estado de BD
    echo ""
    echo "🗄️  Base de datos:"
    if mysql -e "USE $DB_NAME;" 2>/dev/null; then
        TABLES=$(mysql -D $DB_NAME -e "SHOW TABLES;" 2>/dev/null | tail -n +2 | wc -l)
        echo "   ✅ Base de datos '$DB_NAME' existe ($TABLES tablas)"
    else
        echo "   ❌ Base de datos '$DB_NAME' no existe"
    fi
}

# Limpiar proyecto
clean_project() {
    print_header "Limpiando Proyecto"

    print_status "Limpiando archivos compilados..."
    $MVN_CMD clean

    print_status "Limpiando logs..."
    rm -f logs/*.log

    print_status "Limpiando archivos temporales..."
    rm -f server.pid
    rm -f nohup.out

    print_success "✅ Proyecto limpiado"
}

# Menú de ayuda
show_help() {
    show_banner
    echo -e "${YELLOW}Uso: $0 [comando]${NC}"
    echo ""
    echo "🚀 Comandos principales:"
    echo "  ${GREEN}setup${NC}     - Configuración inicial completa (primera vez)"
    echo "  ${GREEN}start${NC}     - Iniciar servidor de desarrollo"
    echo "  ${GREEN}compile${NC}   - Compilar proyecto"
    echo "  ${GREEN}test${NC}      - Ejecutar tests"
    echo ""
    echo "🔧 Comandos de utilidad:"
    echo "  ${CYAN}status${NC}    - Ver estado del proyecto y servicios"
    echo "  ${CYAN}logs${NC}      - Ver logs en tiempo real"
    echo "  ${CYAN}clean${NC}     - Limpiar archivos compilados y temporales"
    echo "  ${CYAN}db-setup${NC}  - Solo configurar base de datos"
    echo ""
    echo "💡 Flujo típico:"
    echo "  1. ${GREEN}$0 setup${NC}    # Primera vez"
    echo "  2. ${GREEN}$0 start${NC}    # Iniciar desarrollo"
    echo "  3. ${CYAN}$0 status${NC}   # Verificar estado"
    echo ""
    echo "🔗 URLs importantes:"
    echo "  📡 API Base: http://localhost:$PORT/api"
    echo "  📊 Health Check: http://localhost:$PORT/api/market/prices"
    echo ""
}

# Función principal
main() {
    case "$1" in
        "setup")
            show_banner
            check_prerequisites
            setup_project
            setup_database
            compile_project
            print_success "🎉 Setup completo. Ejecuta '$0 start' para iniciar el servidor"
            ;;
        "start")
            check_prerequisites
            start_server
            ;;
        "compile")
            check_prerequisites
            compile_project
            ;;
        "test")
            check_prerequisites
            run_tests
            ;;
        "db-setup")
            check_prerequisites
            setup_database
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs
            ;;
        "clean")
            clean_project
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            show_help
            ;;
    esac
}

# Ejecutar función principal
main "$@"