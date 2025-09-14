#!/bin/bash

# Script para parar todos os serviços do PDF Processor API
# Para API, Worker e Redis de forma segura

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
}

echo "🛑 Parando serviços do PDF Processor API..."

# Detectar sistema operacional
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

# Parar API (porta 8080)
log_info "Parando API na porta 8080..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    API_PID=$(lsof -ti:8080)
    kill -TERM $API_PID 2>/dev/null || true
    sleep 3
    
    # Verificar se ainda está rodando
    if ps -p $API_PID > /dev/null 2>&1; then
        log_warning "API não respondeu ao SIGTERM, forçando parada..."
        kill -KILL $API_PID 2>/dev/null || true
    fi
    log_success "API parada"
else
    log_info "API não está rodando na porta 8080"
fi

# Parar Worker (buscar por processo java com worker)
log_info "Parando Worker..."
WORKER_PIDS=$(ps aux | grep -i "worker.*\.jar" | grep -v grep | awk '{print $2}' || true)
if [ -n "$WORKER_PIDS" ]; then
    for PID in $WORKER_PIDS; do
        kill -TERM $PID 2>/dev/null || true
        sleep 2
        
        # Verificar se ainda está rodando
        if ps -p $PID > /dev/null 2>&1; then
            log_warning "Worker (PID: $PID) não respondeu ao SIGTERM, forçando parada..."
            kill -KILL $PID 2>/dev/null || true
        fi
    done
    log_success "Worker(s) parado(s)"
else
    log_info "Worker não está rodando"
fi

# Parar Redis (opcional)
read -p "Deseja parar o Redis também? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    log_info "Parando Redis..."
    
    case $MACHINE in
        Linux)
            if command -v systemctl &> /dev/null; then
                sudo systemctl stop redis-server
                log_success "Redis parado via systemctl"
            elif command -v service &> /dev/null; then
                sudo service redis-server stop
                log_success "Redis parado via service"
            else
                redis-cli shutdown 2>/dev/null || true
                log_success "Redis parado via redis-cli"
            fi
            ;;
        Mac)
            if command -v brew &> /dev/null; then
                brew services stop redis
                log_success "Redis parado via Homebrew"
            else
                redis-cli shutdown 2>/dev/null || true
                log_success "Redis parado via redis-cli"
            fi
            ;;
        *)
            redis-cli shutdown 2>/dev/null || true
            log_success "Redis parado via redis-cli"
            ;;
    esac
else
    log_info "Redis mantido em execução"
fi

# Verificar processos restantes
log_info "Verificando processos restantes..."
REMAINING_PROCESSES=$(ps aux | grep -E "(api.*\.jar|worker.*\.jar)" | grep -v grep || true)
if [ -n "$REMAINING_PROCESSES" ]; then
    log_warning "Processos ainda em execução:"
    echo "$REMAINING_PROCESSES"
else
    log_success "Nenhum processo relacionado encontrado"
fi

echo ""
log_success "🎉 Serviços parados com sucesso!"
echo ""
echo "Para reiniciar:"
echo "  1. Redis: ./scripts/start-redis.sh"
echo "  2. API: ./scripts/run-api.sh"
echo "  3. Worker: ./scripts/run-worker.sh"
