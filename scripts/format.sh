#!/bin/bash

# Script para formatar código do PDF Processor API
# Aplica formatação automática usando Spotless

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

echo "🎨 Formatando código do PDF Processor API..."

# Verificar se Maven está disponível
if ! command -v mvn &> /dev/null; then
    log_error "Maven não está instalado!"
    exit 1
fi

# Aplicar formatação
log_info "Aplicando formatação Spotless..."
mvn spotless:apply -q

if [ $? -eq 0 ]; then
    log_success "Formatação aplicada com sucesso!"
else
    log_error "Erro ao aplicar formatação"
    exit 1
fi

# Verificar se há alterações
if git status --porcelain | grep -q "\.java$"; then
    log_info "Arquivos formatados:"
    git status --porcelain | grep "\.java$" | while read line; do
        echo "  $line"
    done
    echo ""
    log_warning "Não esqueça de fazer commit das alterações!"
else
    log_success "Nenhuma alteração de formatação necessária"
fi

echo ""
log_success "🎉 Formatação concluída!"
