#!/bin/bash

# Script para iniciar Redis local
# Suporta diferentes sistemas operacionais

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

echo "🚀 Iniciando Redis para PDF Processor API..."

# Detectar sistema operacional
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    CYGWIN*)    MACHINE=Cygwin;;
    MINGW*)     MACHINE=MinGw;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

log_info "Sistema detectado: $MACHINE"

# Verificar se Redis está instalado
if ! command -v redis-server &> /dev/null; then
    log_error "Redis não está instalado!"
    echo ""
    echo "Para instalar Redis:"
    case $MACHINE in
        Linux)
            echo "  Ubuntu/Debian: sudo apt update && sudo apt install redis-server"
            echo "  CentOS/RHEL:   sudo dnf install redis"
            ;;
        Mac)
            echo "  Homebrew:      brew install redis"
            echo "  MacPorts:      sudo port install redis"
            ;;
        *)
            echo "  Consulte a documentação oficial: https://redis.io/download"
            ;;
    esac
    exit 1
fi

# Verificar se Redis já está rodando
if redis-cli ping &> /dev/null; then
    log_warning "Redis já está rodando!"
    REDIS_INFO=$(redis-cli info server | grep redis_version)
    echo "  $REDIS_INFO"
    exit 0
fi

# Iniciar Redis baseado no sistema
log_info "Iniciando Redis..."

case $MACHINE in
    Linux)
        if command -v systemctl &> /dev/null; then
            # SystemD (Ubuntu 16+, CentOS 7+)
            sudo systemctl start redis-server
            sudo systemctl enable redis-server
            log_success "Redis iniciado via systemctl"
        elif command -v service &> /dev/null; then
            # SysV Init (sistemas mais antigos)
            sudo service redis-server start
            log_success "Redis iniciado via service"
        else
            # Fallback - iniciar diretamente
            redis-server --daemonize yes
            log_success "Redis iniciado em modo daemon"
        fi
        ;;
    Mac)
        if command -v brew &> /dev/null; then
            # Homebrew
            brew services start redis
            log_success "Redis iniciado via Homebrew"
        else
            # Fallback
            redis-server --daemonize yes
            log_success "Redis iniciado em modo daemon"
        fi
        ;;
    *)
        # Fallback genérico
        redis-server --daemonize yes
        log_success "Redis iniciado em modo daemon"
        ;;
esac

# Aguardar Redis ficar disponível
log_info "Aguardando Redis ficar disponível..."
for i in {1..10}; do
    if redis-cli ping &> /dev/null; then
        log_success "Redis está respondendo!"
        break
    fi
    sleep 1
    if [ $i -eq 10 ]; then
        log_error "Redis não respondeu após 10 segundos"
        exit 1
    fi
done

# Verificar informações do Redis
REDIS_VERSION=$(redis-cli info server | grep redis_version | cut -d: -f2 | tr -d '\r')
REDIS_PORT=$(redis-cli config get port | tail -1)
REDIS_MEMORY=$(redis-cli info memory | grep used_memory_human | cut -d: -f2 | tr -d '\r')

echo ""
log_success "🎉 Redis iniciado com sucesso!"
echo ""
echo "Informações do Redis:"
echo "  Versão: $REDIS_VERSION"
echo "  Porta:  $REDIS_PORT"
echo "  Memória: $REDIS_MEMORY"
echo ""
echo "Para parar Redis:"
case $MACHINE in
    Linux)
        echo "  sudo systemctl stop redis-server"
        ;;
    Mac)
        echo "  brew services stop redis"
        ;;
    *)
        echo "  redis-cli shutdown"
        ;;
esac
