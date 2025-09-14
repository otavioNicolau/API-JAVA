#!/bin/bash

# Script de build do projeto PDF Processor API
# Compila todos os módulos e executa verificações de qualidade

set -e

echo "🔨 Iniciando build do PDF Processor API..."

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log colorido
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

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    log_error "Maven não está instalado. Por favor, instale o Maven primeiro."
    exit 1
fi

# Verificar se Java 21 está instalado
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    log_warning "Java 21 é recomendado. Versão atual: $JAVA_VERSION"
fi

# Limpar build anterior
log_info "Limpando build anterior..."
mvn clean -q

# Compilar projeto
log_info "Compilando projeto..."
mvn compile -q
log_success "Compilação concluída"

# Executar formatação de código
log_info "Formatando código com Spotless..."
mvn spotless:apply -q
log_success "Formatação aplicada"

# Executar verificações de qualidade
log_info "Executando verificações de qualidade..."
mvn checkstyle:check -q
log_success "Checkstyle passou"

# Executar testes
log_info "Executando testes..."
mvn test -q
log_success "Testes executados com sucesso"

# Build final
log_info "Executando build completo..."
mvn package -DskipTests -q
log_success "Build concluído"

# Verificar se os JARs foram criados
API_JAR="app/api/target/api-1.0.0-SNAPSHOT.jar"
WORKER_JAR="app/worker/target/worker-1.0.0-SNAPSHOT.jar"

if [ -f "$API_JAR" ]; then
    log_success "API JAR criado: $API_JAR"
else
    log_warning "API JAR não encontrado: $API_JAR"
fi

if [ -f "$WORKER_JAR" ]; then
    log_success "Worker JAR criado: $WORKER_JAR"
else
    log_warning "Worker JAR não encontrado: $WORKER_JAR"
fi

echo ""
log_success "🎉 Build concluído com sucesso!"
echo ""
echo "Próximos passos:"
echo "  1. Iniciar Redis: ./scripts/start-redis.sh"
echo "  2. Executar API: ./scripts/run-api.sh"
echo "  3. Executar Worker: ./scripts/run-worker.sh"
echo "  4. Acessar documentação: http://localhost:8080/swagger-ui"
