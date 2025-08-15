#!/bin/bash

# daemon.sh - Mantiene el servidor corriendo siempre

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuración
APP_NAME="Trading-Crypto-Server"
LOG_FILE="logs/server.log"
PID_FILE="server.pid"
MAX_RESTARTS=10
RESTART_DELAY=5

print_status() {
    echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

print_error() {
    echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')]${NC} $1"
}

# Crear directorio de logs
mkdir -p logs

# Función para limpiar al salir
cleanup() {
    print_status "Deteniendo $APP_NAME..."
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        kill $PID 2>/dev/null
        rm -f $PID_FILE
    fi
    print_success "$APP_NAME detenido"
    exit 0
}

# Capturar señales para cleanup
trap cleanup SIGTERM SIGINT

# Verificar si ya está corriendo
check_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            print_error "$APP_NAME ya está corriendo (PID: $PID)"
            exit 1
        else
            rm -f $PID_FILE
        fi
    fi
}

# Función para iniciar el servidor
start_server() {
    print_status "Iniciando $APP_NAME..."

    # Verificar Maven
    if command -v mvn &> /dev/null; then
        MVN_CMD="mvn"
    else
        MVN_CMD="./mvnw"
    fi

    # Iniciar en background y capturar PID
    nohup $MVN_CMD spring-boot:run > "$LOG_FILE" 2>&1 &
    SERVER_PID=$!
    echo $SERVER_PID > $PID_FILE

    print_success "Servidor iniciado con PID: $SERVER_PID"
    return $SERVER_PID
}

# Función para monitorear el servidor
monitor_server() {
    local restart_count=0

    while true; do
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)

            # Verificar si el proceso está corriendo
            if ps -p $PID > /dev/null 2>&1; then
                # Verificar si responde en el puerto
                if curl -f http://localhost:8080/api/market/prices > /dev/null 2>&1; then
                    print_success "✅ Servidor funcionando correctamente (PID: $PID)"
                else
                    print_error "⚠️  Servidor no responde en puerto 8080"
                fi
            else
                print_error "❌ Proceso del servidor ha terminado (PID: $PID)"

                if [ $restart_count -lt $MAX_RESTARTS ]; then
                    restart_count=$((restart_count + 1))
                    print_status "🔄 Reiniciando servidor (intento $restart_count/$MAX_RESTARTS)..."

                    rm -f $PID_FILE
                    sleep $RESTART_DELAY
                    start_server
                else
                    print_error "❌ Máximo número de reinicios alcanzado ($MAX_RESTARTS)"
                    exit 1
                fi
            fi
        else
            print_error "❌ Archivo PID no encontrado"
            start_server
        fi

        # Esperar antes del siguiente check
        sleep 30
    done
}

# Comandos
case "$1" in
    "start")
        check_running
        start_server

        print_status "🚀 Iniciando modo daemon..."
        print_status "📊 Servidor: http://localhost:8080"
        print_status "📋 Logs: tail -f $LOG_FILE"
        print_status "🛑 Detener: ./daemon.sh stop"
        print_status "📈 Estado: ./daemon.sh status"

        monitor_server
        ;;

    "stop")
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            print_status "Deteniendo servidor (PID: $PID)..."
            kill $PID
            rm -f $PID_FILE
            print_success "✅ Servidor detenido"
        else
            print_error "❌ Servidor no está corriendo"
        fi
        ;;

    "restart")
        $0 stop
        sleep 3
        $0 start
        ;;

    "status")
        if [ -f "$PID_FILE" ]; then
            PID=$(cat $PID_FILE)
            if ps -p $PID > /dev/null 2>&1; then
                print_success "✅ Servidor corriendo (PID: $PID)"

                # Verificar conectividad
                if curl -f http://localhost:8080/api/market/prices > /dev/null 2>&1; then
                    print_success "🌐 API respondiendo correctamente"
                else
                    print_error "⚠️  API no responde"
                fi

                # Mostrar uso de memoria
                ps -p $PID -o pid,ppid,cmd,%mem,%cpu
            else
                print_error "❌ Proceso no encontrado"
                rm -f $PID_FILE
            fi
        else
            print_error "❌ Servidor no está corriendo"
        fi
        ;;

    "logs")
        if [ -f "$LOG_FILE" ]; then
            tail -f "$LOG_FILE"
        else
            print_error "❌ Archivo de log no encontrado"
        fi
        ;;

    *)
        echo "🚀 Trading Crypto Server - Daemon Control"
        echo ""
        echo "Uso: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "Comandos:"
        echo "  start   - Iniciar servidor en modo daemon (siempre corriendo)"
        echo "  stop    - Detener servidor"
        echo "  restart - Reiniciar servidor"
        echo "  status  - Ver estado del servidor"
        echo "  logs    - Ver logs en tiempo real"
        echo ""
        echo "Ejemplos:"
        echo "  $0 start     # Iniciar modo daemon"
        echo "  $0 status    # Ver si está corriendo"
        echo "  $0 logs      # Ver logs (Ctrl+C para salir)"
        echo "  $0 stop      # Detener completamente"
        ;;
esac