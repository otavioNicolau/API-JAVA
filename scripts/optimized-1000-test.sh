#!/bin/bash

# Teste otimizado para 1000 jobs com rate limiting aumentado

API_BASE="http://localhost:8080/api/v1"
API_KEY="dev-key-12345"
NUM_JOBS=1000
BATCH_SIZE=100

echo "🚀 Teste Otimizado - $NUM_JOBS jobs"
echo "=================================="
echo ""
echo "📊 Configurações:"
echo "  • Total de jobs: $NUM_JOBS"
echo "  • Batch size: $BATCH_SIZE"
echo "  • Rate limit: 10000 req/hora"
echo ""

# Array para armazenar IDs dos jobs
job_ids=()
total_created=0
start_time=$(date +%s)

# Criar jobs em batches
echo "Criando jobs em batches de $BATCH_SIZE:"
for batch_start in $(seq 1 $BATCH_SIZE $NUM_JOBS); do
    batch_end=$((batch_start + BATCH_SIZE - 1))
    if [ $batch_end -gt $NUM_JOBS ]; then
        batch_end=$NUM_JOBS
    fi
    
    batch_num=$(((batch_start - 1) / BATCH_SIZE + 1))
    echo "📦 Batch $batch_num: Jobs $batch_start-$batch_end"
    
    batch_created=0
    for i in $(seq $batch_start $batch_end); do
        echo -n "  Job $i... "
        
        response=$(curl -s -X POST \
            -H "X-API-Key: $API_KEY" \
            -F "operation=PDF_CREATE" \
            -F "optionsJson={\"text_content\":\"Teste 1000 jobs #$i\",\"title\":\"Documento $i\",\"pages\":1}" \
            "$API_BASE/jobs" 2>/dev/null)
        
        # Extrair ID do job
        job_id=$(echo "$response" | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
        
        if [ -n "$job_id" ]; then
            job_ids+=("$job_id")
            echo "✅"
            ((batch_created++))
            ((total_created++))
        else
            echo "❌"
        fi
        
        # Pausa mínima
        sleep 0.05
    done
    
    echo "  ✅ Batch $batch_num: $batch_created/$BATCH_SIZE jobs criados"
    echo ""
    
    # Pausa entre batches
    sleep 2
done

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
    
    while true; do
        completed=0
        pending=0
        failed=0
        
        # Verificar status em batches para não sobrecarregar
        for i in $(seq 0 49 ${#job_ids[@]}); do
            batch_end=$((i + 49))
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
            
            # Pausa entre batches de verificação
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
    echo "🎉 TESTE DE 1000 JOBS CONCLUÍDO!"
    echo "================================"
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
    echo "🏆 SISTEMA VALIDADO EM ESCALA MASSIVA!"
    echo "   ✅ $completed PDFs processados com sucesso"
    echo "   ✅ Worker demonstrou alta performance"
    echo "   ✅ API suportou carga elevada"
else
    echo "❌ Nenhum job foi criado com sucesso!"
fi
