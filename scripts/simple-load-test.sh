#!/bin/bash

# Teste de carga simples - criar jobs sequencialmente e monitorar

API_BASE="http://localhost:8080/api/v1"
API_KEY="dev-key-12345"
NUM_JOBS=1000

echo "🚀 Criando $NUM_JOBS jobs para teste de performance do Worker..."
echo ""

# Array para armazenar IDs dos jobs
job_ids=()

# Criar jobs
echo "Criando jobs:"
for i in $(seq 1 $NUM_JOBS); do
    echo -n "Job $i... "
    
    response=$(curl -s -X POST \
        -H "X-API-Key: $API_KEY" \
        -F "operation=PDF_CREATE" \
        -F "optionsJson={\"text_content\":\"Job de teste #$i\",\"title\":\"Documento $i\",\"pages\":1}" \
        "$API_BASE/jobs" 2>/dev/null)
    
    # Extrair ID do job
    job_id=$(echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    
    if [ -n "$job_id" ]; then
        job_ids+=("$job_id")
        echo "✅ $job_id"
    else
        echo "❌ ERRO"
    fi
    
    # Pequena pausa para não sobrecarregar
    sleep 0.1
done

echo ""
echo "📊 ${#job_ids[@]} jobs criados com sucesso!"
echo ""

# Monitorar progresso
echo "Monitorando processamento..."
start_time=$(date +%s)

while true; do
    completed=0
    pending=0
    failed=0
    
    for job_id in "${job_ids[@]}"; do
        status_response=$(curl -s -H "X-API-Key: $API_KEY" "$API_BASE/jobs/$job_id" 2>/dev/null)
        status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        
        case $status in
            "COMPLETED") ((completed++)) ;;
            "PENDING"|"PROCESSING") ((pending++)) ;;
            "FAILED"|"ERROR") ((failed++)) ;;
        esac
    done
    
    current_time=$(date +%s)
    elapsed=$((current_time - start_time))
    
    echo "$(date '+%H:%M:%S') - Concluídos: $completed | Pendentes: $pending | Falharam: $failed | Tempo: ${elapsed}s"
    
    # Parar quando todos estiverem processados
    if [ $pending -eq 0 ]; then
        break
    fi
    
    sleep 5
done

total_time=$(($(date +%s) - start_time))

echo ""
echo "🎉 Teste concluído!"
echo "📊 Estatísticas:"
echo "  • Jobs processados: $completed"
echo "  • Jobs falharam: $failed"
echo "  • Tempo total: ${total_time}s"
echo "  • Taxa de processamento: $(echo "scale=2; $completed / $total_time" | bc -l) jobs/s"
