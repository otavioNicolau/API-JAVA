#!/bin/bash

# Script para executar verificações de qualidade de código
# Executa Checkstyle, SpotBugs e outras verificações

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

echo "🔍 Executando verificações de qualidade de código..."

# Verificar se Maven está disponível
if ! command -v mvn &> /dev/null; then
    log_error "Maven não está instalado!"
    exit 1
fi

EXIT_CODE=0

# Verificar formatação
log_info "Verificando formatação de código..."
if mvn spotless:check -q; then
    log_success "Formatação está correta"
else
    log_error "Formatação incorreta. Execute: ./scripts/format.sh"
    EXIT_CODE=1
fi

# Executar Checkstyle
log_info "Executando Checkstyle..."
if mvn checkstyle:check -q; then
    log_success "Checkstyle passou"
else
    log_error "Checkstyle encontrou problemas"
    EXIT_CODE=1
fi

# Executar SpotBugs
log_info "Executando SpotBugs..."
if mvn spotbugs:check -q; then
    log_success "SpotBugs passou"
else
    log_warning "SpotBugs encontrou alguns problemas"
    # SpotBugs warnings não falham o build por padrão
fi

# Compilar para verificar erros
log_info "Verificando compilação..."
if mvn compile -q; then
    log_success "Compilação bem-sucedida"
else
    log_error "Erro de compilação"
    EXIT_CODE=1
fi

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    log_success "🎉 Todas as verificações passaram!"
else
    log_error "❌ Algumas verificações falharam"
fi

echo ""
echo "Relatórios gerados:"
echo "  Checkstyle: target/site/checkstyle.html"
echo "  SpotBugs: target/spotbugsXml.xml"

exit $EXIT_CODE
