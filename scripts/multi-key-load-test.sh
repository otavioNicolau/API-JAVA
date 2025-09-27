#!/bin/bash

# Teste de carga com múltiplas API keys para contornar rate limiting

API_BASE="http://localhost:8080/api/v1"
NUM_JOBS=1000

# API keys válidas configuradas no sistema
API_KEYS=(
    "dev-key-12345"
    "test-key-67890" 
    "prod-key-abcdef"
)

echo "🚀 Teste de Carga com Múltiplas API Keys - $NUM_JOBS jobs"
echo "========================================================="
echo ""
echo "📊 Configurações:"
echo "  • Total de jobs: $NUM_JOBS"
echo "  • API Keys disponíveis: ${#API_KEYS[@]}"
echo "  • Jobs por key: $((NUM_JOBS / ${#API_KEYS[@]}))"
echo ""

# Array para armazenar IDs dos jobs
job_ids=()
total_created=0
start_time=$(date +%s)

# Função para criar jobs com uma API key específica
create_jobs_with_key() {
    local api_key=$1
    local start_job=$2
    local end_job=$3
    local key_created=0
    
    echo "🔑 Usando API Key: $api_key (Jobs $start_job-$end_job)"
    
    for i in $(seq $start_job $end_job); do
        echo -n "  Job $i... "
        
        response=$(curl -s -X POST \
            -H "X-API-Key: $api_key" \
            -F "operation=PDF_CREATE" \
            -F "optionsJson={\"text_content\":\"Teste massivo #$i\",\"title\":\"Doc $i\",\"pages\":1}" \
            "$API_BASE/jobs" 2>/dev/null)
        
        # Extrair ID do job
        job_id=$(echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$job_id" ]; then
            job_ids+=("$job_id")
            echo "✅ $job_id"
            ((key_created++))
            ((total_created++))
        else
            echo "❌ ERRO"
        fi
        
        # Pausa mínima entre requisições
        sleep 0.1
    done
    
    echo "  ✅ Concluído com $api_key: $key_created jobs criados"
    echo ""
}

# Distribuir jobs entre as API keys
jobs_per_key=$((NUM_JOBS / ${#API_KEYS[@]}))
remaining_jobs=$((NUM_JOBS % ${#API_KEYS[@]}))

current_job=1
for i in "${!API_KEYS[@]}"; do
    api_key="${API_KEYS[$i]}"
    
    # Calcular range de jobs para esta key
    jobs_for_this_key=$jobs_per_key
    if [ $i -lt $remaining_jobs ]; then
        ((jobs_for_this_key++))
    fi
    
    end_job=$((current_job + jobs_for_this_key - 1))
    
    # Criar jobs em paralelo para acelerar
    create_jobs_with_key "$api_key" $current_job $end_job &
    
    current_job=$((end_job + 1))
done

# Aguardar todos os processos paralelos terminarem
wait

creation_time=$(($(date +%s) - start_time))

echo "🎉 Criação concluída!"
echo "📊 Estatísticas de Criação:"
echo "  • Jobs criados: $total_created de $NUM_JOBS"
echo "  • Tempo de criação: ${creation_time}s"
if [ $creation_time -gt 0 ]; then
    echo "  • Taxa de criação: $(echo "scale=2; $total_created / $creation_time" | bc -l) jobs/s"
fi
echo ""

# Monitorar processamento
if [ ${#job_ids[@]} -gt 0 ]; then
    echo "📊 Monitorando processamento de ${#job_ids[@]} jobs..."
    monitor_start=$(date +%s)
    
    # Usar a primeira API key para monitoramento
    monitor_key="${API_KEYS[0]}"
    
    while true; do
        completed=0
        pending=0
        failed=0
        
        # Verificar status em batches pequenos para não sobrecarregar
        for i in $(seq 0 19 ${#job_ids[@]}); do
            batch_end=$((i + 19))
            if [ $batch_end -ge ${#job_ids[@]} ]; then
                batch_end=$((${#job_ids[@]} - 1))
            fi
            
            for j in $(seq $i $batch_end); do
                if [ $j -lt ${#job_ids[@]} ]; then
                    job_id=${job_ids[$j]}
                    status_response=$(curl -s -H "X-API-Key: $monitor_key" "$API_BASE/jobs/$job_id" 2>/dev/null)
                    status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
                    
                    case $status in
                        "COMPLETED") ((completed++)) ;;
                        "PENDING"|"PROCESSING") ((pending++)) ;;
                        "FAILED"|"ERROR") ((failed++)) ;;
                    esac
                fi
            done
            
            # Pausa entre batches de verificação
            sleep 1
        done
        
        current_time=$(date +%s)
        elapsed=$((current_time - monitor_start))
        
        echo "$(date '+%H:%M:%S') - Concluídos: $completed | Pendentes: $pending | Falharam: $failed | Tempo: ${elapsed}s"
        
        # Parar quando todos estiverem processados
        if [ $pending -eq 0 ]; then
            break
        fi
        
        sleep 15
    done
    
    total_time=$(($(date +%s) - start_time))
    processing_time=$(($(date +%s) - monitor_start))
    
    echo ""
    echo "🎉 TESTE MASSIVO DE 1000 JOBS CONCLUÍDO!"
    echo "========================================"
    echo "📊 Estatísticas Finais:"
    echo "  • Jobs criados: $total_created"
    echo "  • Jobs processados: $completed"
    echo "  • Jobs falharam: $failed"
    echo "  • Tempo total: ${total_time}s"
    echo "  • Tempo de processamento: ${processing_time}s"
    if [ $processing_time -gt 0 ]; then
        echo "  • Taxa de processamento: $(echo "scale=2; $completed / $processing_time" | bc -l) jobs/s"
    fi
    if [ $total_time -gt 0 ]; then
        echo "  • Taxa geral: $(echo "scale=2; $completed / $total_time" | bc -l) jobs/s"
    fi
    echo ""
    echo "🏆 SISTEMA TESTADO COM SUCESSO EM ESCALA MASSIVA!"
else
    echo "❌ Nenhum job foi criado com sucesso!"
fi
