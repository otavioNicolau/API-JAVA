#!/bin/bash

# Teste de carga massivo - 1000 jobs com rate limiting inteligente

API_BASE="http://localhost:8080/api/v1"
API_KEY="dev-key-12345"
NUM_JOBS=1000
BATCH_SIZE=50
DELAY_BETWEEN_BATCHES=60  # 1 minuto entre batches para respeitar rate limit

echo "🚀 Teste de Carga Massivo - $NUM_JOBS jobs"
echo "======================================"
echo ""
echo "📊 Configurações:"
echo "  • Total de jobs: $NUM_JOBS"
echo "  • Tamanho do batch: $BATCH_SIZE"
echo "  • Delay entre batches: ${DELAY_BETWEEN_BATCHES}s"
echo ""

# Array para armazenar IDs dos jobs
job_ids=()
total_created=0
batch_num=1

# Função para criar um batch de jobs
create_batch() {
    local start_job=$1
    local end_job=$2
    local batch_created=0
    
    echo "📦 Batch $batch_num: Jobs $start_job-$end_job"
    
    for i in $(seq $start_job $end_job); do
        echo -n "  Job $i... "
        
        response=$(curl -s -X POST \
            -H "X-API-Key: $API_KEY" \
            -F "operation=PDF_CREATE" \
            -F "optionsJson={\"text_content\":\"Teste de carga massivo #$i\",\"title\":\"Documento $i\",\"pages\":1}" \
            "$API_BASE/jobs" 2>/dev/null)
        
        # Extrair ID do job
        job_id=$(echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$job_id" ]; then
            job_ids+=("$job_id")
            echo "✅ $job_id"
            ((batch_created++))
            ((total_created++))
        else
            echo "❌ ERRO"
            # Se houver erro, aguardar um pouco mais
            sleep 1
        fi
        
        # Pequena pausa entre jobs
        sleep 0.2
    done
    
    echo "  ✅ Batch $batch_num concluído: $batch_created jobs criados"
    echo ""
    
    ((batch_num++))
}

# Criar jobs em batches
start_time=$(date +%s)

for batch_start in $(seq 1 $BATCH_SIZE $NUM_JOBS); do
    batch_end=$((batch_start + BATCH_SIZE - 1))
    if [ $batch_end -gt $NUM_JOBS ]; then
        batch_end=$NUM_JOBS
    fi
    
    create_batch $batch_start $batch_end
    
    # Se não é o último batch, aguardar antes do próximo
    if [ $batch_end -lt $NUM_JOBS ]; then
        echo "⏳ Aguardando ${DELAY_BETWEEN_BATCHES}s antes do próximo batch..."
        echo "   (Total criado até agora: $total_created jobs)"
        echo ""
        sleep $DELAY_BETWEEN_BATCHES
    fi
done

creation_time=$(($(date +%s) - start_time))

echo "🎉 Criação concluída!"
echo "📊 Estatísticas de Criação:"
echo "  • Jobs criados: $total_created de $NUM_JOBS"
echo "  • Tempo de criação: ${creation_time}s"
echo "  • Taxa de criação: $(echo "scale=2; $total_created / $creation_time" | bc -l) jobs/s"
echo ""

# Monitorar processamento
if [ ${#job_ids[@]} -gt 0 ]; then
    echo "📊 Monitorando processamento de ${#job_ids[@]} jobs..."
    monitor_start=$(date +%s)
    
    while true; do
        completed=0
        pending=0
        failed=0
        
        # Verificar status em batches para não sobrecarregar a API
        for i in $(seq 0 9 ${#job_ids[@]}); do
            batch_end=$((i + 9))
            if [ $batch_end -ge ${#job_ids[@]} ]; then
                batch_end=$((${#job_ids[@]} - 1))
            fi
            
            for j in $(seq $i $batch_end); do
                if [ $j -lt ${#job_ids[@]} ]; then
                    job_id=${job_ids[$j]}
                    status_response=$(curl -s -H "X-API-Key: $API_KEY" "$API_BASE/jobs/$job_id" 2>/dev/null)
                    status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
                    
                    case $status in
                        "COMPLETED") ((completed++)) ;;
                        "PENDING"|"PROCESSING") ((pending++)) ;;
                        "FAILED"|"ERROR") ((failed++)) ;;
                    esac
                fi
            done
            
            # Pequena pausa entre batches de verificação
            sleep 0.5
        done
        
        current_time=$(date +%s)
        elapsed=$((current_time - monitor_start))
        
        echo "$(date '+%H:%M:%S') - Concluídos: $completed | Pendentes: $pending | Falharam: $failed | Tempo: ${elapsed}s"
        
        # Parar quando todos estiverem processados
        if [ $pending -eq 0 ]; then
            break
        fi
        
        sleep 10
    done
    
    total_time=$(($(date +%s) - start_time))
    processing_time=$(($(date +%s) - monitor_start))
    
    echo ""
    echo "🎉 Teste Massivo Concluído!"
    echo "📊 Estatísticas Finais:"
    echo "  • Jobs criados: $total_created"
    echo "  • Jobs processados: $completed"
    echo "  • Jobs falharam: $failed"
    echo "  • Tempo total: ${total_time}s"
    echo "  • Tempo de processamento: ${processing_time}s"
    echo "  • Taxa de processamento: $(echo "scale=2; $completed / $processing_time" | bc -l) jobs/s"
    echo "  • Taxa geral: $(echo "scale=2; $completed / $total_time" | bc -l) jobs/s"
else
    echo "❌ Nenhum job foi criado com sucesso!"
fi
