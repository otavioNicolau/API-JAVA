#!/bin/bash

# Script para executar o Worker do PDF Processor
# Inicia o processador assíncrono de jobs

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

echo "⚙️ Iniciando PDF Processor Worker..."

# Verificar se Redis está rodando
if ! redis-cli ping &> /dev/null; then
    log_error "Redis não está rodando!"
    echo "Execute primeiro: ./scripts/start-redis.sh"
    exit 1
fi

# Verificar se o JAR do Worker existe
WORKER_JAR="app/worker/target/worker-1.0.0-SNAPSHOT.jar"
if [ ! -f "$WORKER_JAR" ]; then
    log_warning "JAR do Worker não encontrado. Executando build..."
    ./scripts/build.sh
fi

# Configurar variáveis de ambiente
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}
export REDIS_HOST=${REDIS_HOST:-"localhost"}
export REDIS_PORT=${REDIS_PORT:-6379}
export WORKER_THREADS=${WORKER_THREADS:-2}

log_info "Configurações:"
echo "  Profile: $SPRING_PROFILES_ACTIVE"
echo "  Redis: $REDIS_HOST:$REDIS_PORT"
echo "  Threads: $WORKER_THREADS"
echo ""

# Criar diretórios necessários se não existirem
mkdir -p storage/jobs
mkdir -p storage/results
mkdir -p storage/temp

log_info "Iniciando Worker..."
echo ""

# Executar o Worker
java -jar "$WORKER_JAR" \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE \
    --spring.data.redis.host=$REDIS_HOST \
    --spring.data.redis.port=$REDIS_PORT \
    --worker.threads=$WORKER_THREADS \
    --logging.level.com.pdfprocessor=DEBUG &

WORKER_PID=$!

# Aguardar Worker ficar disponível
log_info "Aguardando Worker inicializar..."
sleep 5

# Verificar se o processo ainda está rodando
if ps -p $WORKER_PID > /dev/null; then
    log_success "Worker está rodando!"
else
    log_error "Worker falhou ao iniciar"
    exit 1
fi

echo ""
log_success "🎉 PDF Processor Worker iniciado com sucesso!"
echo ""
echo "Worker Status:"
echo "  PID: $WORKER_PID"
echo "  Threads: $WORKER_THREADS"
echo "  Redis Queue: pdf-jobs"
echo ""
echo "Para monitorar jobs:"
echo "  redis-cli LLEN pdf-jobs"
echo "  redis-cli LRANGE pdf-jobs 0 -1"
echo ""
echo "Para parar o Worker: kill $WORKER_PID"
echo ""

# Manter o script rodando
wait $WORKER_PID
