#!/bin/bash

# Script para executar testes do PDF Processor API
# Executa testes unitários, integração e verificações de qualidade

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

echo "🧪 Executando testes do PDF Processor API..."

# Verificar se Maven está disponível
if ! command -v mvn &> /dev/null; then
    log_error "Maven não está instalado!"
    exit 1
fi

# Função para executar comando com tratamento de erro
run_command() {
    local cmd="$1"
    local description="$2"
    
    log_info "$description..."
    if eval "$cmd"; then
        log_success "$description concluído"
    else
        log_error "$description falhou"
        return 1
    fi
}

# Limpar builds anteriores
run_command "mvn clean -q" "Limpando builds anteriores"

# Compilar projeto
run_command "mvn compile -q" "Compilando projeto"

# Executar formatação de código
run_command "mvn spotless:check -q" "Verificando formatação de código"

# Executar Checkstyle
run_command "mvn checkstyle:check -q" "Executando Checkstyle"

# Executar testes unitários
log_info "Executando testes unitários..."
mvn test -q
TEST_RESULT=$?

if [ $TEST_RESULT -eq 0 ]; then
    log_success "Testes unitários passaram"
else
    log_error "Alguns testes falharam"
fi

# Executar testes de integração (se existirem)
log_info "Executando testes de integração..."
mvn verify -DskipUnitTests=true -q
INTEGRATION_RESULT=$?

if [ $INTEGRATION_RESULT -eq 0 ]; then
    log_success "Testes de integração passaram"
else
    log_warning "Alguns testes de integração falharam"
fi

# Gerar relatório de cobertura
log_info "Gerando relatório de cobertura..."
mvn jacoco:report -q
log_success "Relatório de cobertura gerado"

# Executar SpotBugs (análise estática)
log_info "Executando análise estática (SpotBugs)..."
mvn spotbugs:check -q
SPOTBUGS_RESULT=$?

if [ $SPOTBUGS_RESULT -eq 0 ]; then
    log_success "Análise estática passou"
else
    log_warning "SpotBugs encontrou alguns problemas"
fi

echo ""
echo "📊 Resumo dos Testes:"
echo "===================="

# Contar testes
TOTAL_TESTS=$(find . -name "TEST-*.xml" -exec grep -l "testcase" {} \; 2>/dev/null | wc -l)
if [ $TOTAL_TESTS -gt 0 ]; then
    echo "  Testes executados: $TOTAL_TESTS arquivos de teste"
else
    echo "  Testes executados: Verificar logs do Maven"
fi

# Status geral
if [ $TEST_RESULT -eq 0 ] && [ $INTEGRATION_RESULT -eq 0 ]; then
    echo "  Status geral: ✅ PASSOU"
    EXIT_CODE=0
elif [ $TEST_RESULT -eq 0 ]; then
    echo "  Status geral: ⚠️  PARCIAL (testes unitários OK)"
    EXIT_CODE=1
else
    echo "  Status geral: ❌ FALHOU"
    EXIT_CODE=1
fi

echo ""
echo "Relatórios gerados:"
echo "  Cobertura: target/site/jacoco/index.html"
echo "  Checkstyle: target/site/checkstyle.html"
echo "  SpotBugs: target/spotbugsXml.xml"

if [ $EXIT_CODE -eq 0 ]; then
    log_success "🎉 Todos os testes passaram!"
else
    log_warning "⚠️  Alguns testes falharam. Verifique os logs acima."
fi

exit $EXIT_CODE
