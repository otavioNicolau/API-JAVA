#!/bin/bash

# Script para executar a API do PDF Processor
# Inicia o servidor Spring Boot da API REST

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

echo "🚀 Iniciando PDF Processor API..."

# Verificar se Redis está rodando
if ! redis-cli ping &> /dev/null; then
    log_error "Redis não está rodando!"
    echo "Execute primeiro: ./scripts/start-redis.sh"
    exit 1
fi

# Verificar se o JAR da API existe
API_JAR="app/api/target/api-1.0.0-SNAPSHOT.jar"
if [ ! -f "$API_JAR" ]; then
    log_warning "JAR da API não encontrado. Executando build..."
    ./scripts/build.sh
fi

# Verificar se a porta 8080 está livre
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_error "Porta 8080 já está em uso!"
    echo "Para parar processo existente:"
    echo "  lsof -ti:8080 | xargs kill -9"
    exit 1
fi

# Configurar variáveis de ambiente
export API_KEY=${API_KEY:-"dev123"}
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}
export SERVER_PORT=${SERVER_PORT:-8080}
export REDIS_HOST=${REDIS_HOST:-"localhost"}
export REDIS_PORT=${REDIS_PORT:-6379}

log_info "Configurações:"
echo "  API Key: $API_KEY"
echo "  Profile: $SPRING_PROFILES_ACTIVE"
echo "  Porta: $SERVER_PORT"
echo "  Redis: $REDIS_HOST:$REDIS_PORT"
echo ""

# Criar diretório de storage se não existir
mkdir -p storage/jobs
mkdir -p storage/results

log_info "Iniciando API..."
echo ""

# Executar a API
java -jar "$API_JAR" \
    --server.port=$SERVER_PORT \
    --spring.profiles.active=$SPRING_PROFILES_ACTIVE \
    --spring.data.redis.host=$REDIS_HOST \
    --spring.data.redis.port=$REDIS_PORT \
    --logging.level.com.pdfprocessor=DEBUG \
    --management.endpoints.web.exposure.include=health,metrics,info &

API_PID=$!

# Aguardar API ficar disponível
log_info "Aguardando API ficar disponível..."
for i in {1..30}; do
    if curl -s -f http://localhost:$SERVER_PORT/actuator/health > /dev/null 2>&1; then
        log_success "API está respondendo!"
        break
    fi
    sleep 2
    if [ $i -eq 30 ]; then
        log_error "API não respondeu após 60 segundos"
        kill $API_PID 2>/dev/null || true
        exit 1
    fi
done

echo ""
log_success "🎉 PDF Processor API iniciada com sucesso!"
echo ""
echo "Endpoints disponíveis:"
echo "  API Base:      http://localhost:$SERVER_PORT/api/v1"
echo "  Swagger UI:    http://localhost:$SERVER_PORT/swagger-ui"
echo "  Health Check:  http://localhost:$SERVER_PORT/actuator/health"
echo "  Metrics:       http://localhost:$SERVER_PORT/actuator/metrics"
echo ""
echo "Exemplo de uso:"
echo "  curl -H \"X-API-Key: $API_KEY\" http://localhost:$SERVER_PORT/actuator/health"
echo ""
echo "Para parar a API: kill $API_PID"
echo ""

# Manter o script rodando
wait $API_PID
