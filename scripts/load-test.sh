#!/bin/bash

# Script para teste de carga - criar 100 jobs e monitorar performance

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

# Configurações
API_BASE="http://localhost:8080/api/v1"
API_KEY="dev-key-12345"
NUM_JOBS=100
RESULTS_FILE="load_test_results.txt"

echo "🚀 Teste de Carga - PDF Processor API"
echo "======================================"
echo ""
echo "📊 Configurações:"
echo "  • API: $API_BASE"
echo "  • Número de jobs: $NUM_JOBS"
echo "  • Arquivo de resultados: $RESULTS_FILE"
echo ""

# Verificar se a API está disponível
log_info "Verificando disponibilidade da API..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -H "X-API-Key: $API_KEY" "$API_BASE/../actuator/health")
if [ "$HTTP_STATUS" != "200" ]; then
    log_error "API não está disponível em $API_BASE (Status: $HTTP_STATUS)"
    exit 1
fi
log_success "API está disponível (Status: $HTTP_STATUS)"

# Limpar arquivo de resultados anterior
> $RESULTS_FILE

# Função para criar um job
create_job() {
    local job_num=$1
    local start_time=$(date +%s.%N)
    
    local response=$(curl -s -X POST \
        -H "X-API-Key: $API_KEY" \
        -F "operation=PDF_CREATE" \
        -F "optionsJson={\"text_content\":\"Job de teste #$job_num - Teste de carga da API\",\"title\":\"Documento $job_num\",\"author\":\"Load Test\",\"pages\":1}" \
        "$API_BASE/jobs")
    
    local end_time=$(date +%s.%N)
    local duration=$(echo "$end_time - $start_time" | bc -l)
    
    # Extrair ID do job da resposta
    local job_id=$(echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$job_id" ]; then
        echo "Job #$job_num: $job_id (${duration}s)" >> $RESULTS_FILE
        echo -n "."
    else
        echo "Job #$job_num: ERRO - $response" >> $RESULTS_FILE
        echo -n "X"
    fi
}

# Criar jobs em paralelo
log_info "Criando $NUM_JOBS jobs..."
echo "Progresso: "

START_TIME=$(date +%s.%N)

# Criar jobs em lotes para não sobrecarregar
BATCH_SIZE=10
for ((i=1; i<=NUM_JOBS; i+=BATCH_SIZE)); do
    # Criar lote de jobs em paralelo
    for ((j=0; j<BATCH_SIZE && (i+j)<=NUM_JOBS; j++)); do
        create_job $((i+j)) &
    done
    
    # Aguardar conclusão do lote
    wait
    
    # Mostrar progresso
    local progress=$((i+BATCH_SIZE-1))
    if [ $progress -gt $NUM_JOBS ]; then
        progress=$NUM_JOBS
    fi
    echo " [$progress/$NUM_JOBS]"
done

END_TIME=$(date +%s.%N)
TOTAL_DURATION=$(echo "$END_TIME - $START_TIME" | bc -l)

echo ""
log_success "Todos os jobs foram criados!"
echo ""

# Estatísticas de criação
log_info "Estatísticas de criação de jobs:"
echo "  • Tempo total: ${TOTAL_DURATION}s"
echo "  • Jobs por segundo: $(echo "scale=2; $NUM_JOBS / $TOTAL_DURATION" | bc -l)"

# Aguardar um pouco e verificar status dos jobs
log_info "Aguardando processamento..."
sleep 5

# Verificar status dos jobs
log_info "Verificando status dos jobs..."

COMPLETED=0
PENDING=0
FAILED=0

while IFS= read -r line; do
    if [[ $line == Job* ]]; then
        job_id=$(echo "$line" | cut -d' ' -f3)
        if [[ $job_id != "ERRO" ]]; then
            status_response=$(curl -s -H "X-API-Key: $API_KEY" "$API_BASE/jobs/$job_id" 2>/dev/null)
            status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
            
            case $status in
                "COMPLETED") ((COMPLETED++)) ;;
                "PENDING"|"PROCESSING") ((PENDING++)) ;;
                "FAILED"|"ERROR") ((FAILED++)) ;;
            esac
        else
            ((FAILED++))
        fi
    fi
done < $RESULTS_FILE

echo ""
log_info "Status atual dos jobs:"
echo "  ✅ Concluídos: $COMPLETED"
echo "  ⏳ Pendentes/Processando: $PENDING"
echo "  ❌ Falharam: $FAILED"

# Monitoramento contínuo
log_info "Monitorando progresso (pressione Ctrl+C para parar)..."

monitor_progress() {
    local prev_completed=0
    local start_monitor=$(date +%s)
    
    while true; do
        sleep 10
        
        local current_completed=0
        local current_pending=0
        local current_failed=0
        
        while IFS= read -r line; do
            if [[ $line == Job* ]]; then
                job_id=$(echo "$line" | cut -d' ' -f3)
                if [[ $job_id != "ERRO" ]]; then
                    status_response=$(curl -s -H "X-API-Key: $API_KEY" "$API_BASE/jobs/$job_id" 2>/dev/null)
                    status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
                    
                    case $status in
                        "COMPLETED") ((current_completed++)) ;;
                        "PENDING"|"PROCESSING") ((current_pending++)) ;;
                        "FAILED"|"ERROR") ((current_failed++)) ;;
                    esac
                else
                    ((current_failed++))
                fi
            fi
        done < $RESULTS_FILE
        
        local current_time=$(date +%s)
        local elapsed=$((current_time - start_monitor))
        local jobs_per_sec=0
        if [ $elapsed -gt 0 ]; then
            jobs_per_sec=$(echo "scale=2; ($current_completed - $prev_completed) / 10" | bc -l)
        fi
        
        echo "$(date '+%H:%M:%S') - Concluídos: $current_completed | Pendentes: $current_pending | Falharam: $current_failed | Velocidade: ${jobs_per_sec} jobs/s"
        
        prev_completed=$current_completed
        
        # Parar quando todos estiverem concluídos ou falharam
        if [ $current_pending -eq 0 ]; then
            log_success "Todos os jobs foram processados!"
            break
        fi
    done
}

# Executar monitoramento
monitor_progress

# Relatório final
echo ""
log_success "🎉 Teste de carga concluído!"
echo ""
echo "📊 Relatório Final:"
echo "  • Jobs criados: $NUM_JOBS"
echo "  • Tempo de criação: ${TOTAL_DURATION}s"
echo "  • Taxa de criação: $(echo "scale=2; $NUM_JOBS / $TOTAL_DURATION" | bc -l) jobs/s"
echo ""
echo "📁 Detalhes salvos em: $RESULTS_FILE"
