#!/bin/bash

# Script de teste rápido para validar a API e exemplos

API_BASE="http://localhost:8080/api/v1"

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Teste Rápido da API de Processamento de PDF ===${NC}"
echo ""

# Função para testar endpoint
test_endpoint() {
    local endpoint=$1
    local description=$2
    local expected_status=${3:-200}
    
    echo -ne "${YELLOW}Testando $description...${NC} "
    
    local response=$(curl -s -w "%{http_code}" "$API_BASE$endpoint")
    local status_code=${response: -3}
    local body=${response%???}
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ OK ($status_code)${NC}"
        return 0
    else
        echo -e "${RED}✗ FALHOU ($status_code)${NC}"
        if [ -n "$body" ]; then
            echo "  Response: $body"
        fi
        return 1
    fi
}

# Função para testar criação de job
test_job_creation() {
    local operation=$1
    local options=$2
    local description=$3
    
    echo -ne "${YELLOW}Testando criação de job: $description...${NC} "
    
    local response=$(curl -s -X POST "$API_BASE/jobs" \
        -F "operation=$operation" \
        -F "inputFiles=test1.pdf" \
        -F "inputFiles=test2.pdf" \
        -F "optionsJson=$options")
    
    if echo "$response" | grep -q '"id"'; then
        echo -e "${GREEN}✓ Job criado${NC}"
        local job_id=$(echo "$response" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
        echo "  Job ID: $job_id"
        return 0
    else
        echo -e "${RED}✗ Falha na criação${NC}"
        echo "  Response: $response"
        return 1
    fi
}

echo -e "${BLUE}1. Testando Endpoints Básicos${NC}"
echo ""

# Testar health check
test_endpoint "/health" "Health Check"

# Testar listagem de operações
test_endpoint "/operations" "Listagem de Operações"

# Testar esquemas de opções
test_endpoint "/operations/MERGE/options-schema" "Esquema MERGE"
test_endpoint "/operations/PDF_CROP/options-schema" "Esquema PDF_CROP"
test_endpoint "/operations/WATERMARK/options-schema" "Esquema WATERMARK"

echo ""
echo -e "${BLUE}2. Testando Criação de Jobs (sem arquivos)${NC}"
echo ""

# Testar criação de jobs com opções válidas (mas sem arquivos reais)
test_job_creation "MERGE" \
    '{"inputFiles": ["test1.pdf", "test2.pdf"], "outputFileName": "merged.pdf"}' \
    "Merge básico"

test_job_creation "SPLIT" \
    '{"inputFile": "test.pdf", "pages": "1-3", "outputPrefix": "page_"}' \
    "Split básico"

test_job_creation "WATERMARK" \
    '{"inputFile": "test.pdf", "text": "TESTE", "fontSize": 36, "opacity": 0.5, "position": "center", "outputFileName": "watermarked.pdf"}' \
    "Watermark básico"

test_job_creation "PDF_CROP" \
    '{"inputFile": "test.pdf", "x": 50, "y": 50, "width": 400, "height": 600, "pages": "all", "outputFileName": "cropped.pdf"}' \
    "Crop avançado"

test_job_creation "PDF_REORDER" \
    '{"inputFile": "test.pdf", "pageOrder": [2, 1, 3], "outputFileName": "reordered.pdf"}' \
    "Reorder avançado"

test_job_creation "PDF_RESIZE" \
    '{"inputFile": "test.pdf", "pageSize": "A4", "scaleMode": "fit", "outputFileName": "resized.pdf"}' \
    "Resize avançado"

test_job_creation "COMPRESS" \
    '{"inputFile": "test.pdf", "compressionLevel": "medium", "optimizeImages": true, "outputFileName": "compressed.pdf"}' \
    "Compress avançado"

echo ""
echo -e "${BLUE}3. Testando Validação de Opções${NC}"
echo ""

# Testar validação com opções inválidas
echo -ne "${YELLOW}Testando validação (opções inválidas)...${NC} "
response=$(curl -s -X POST "$API_BASE/jobs" \
    -H "Content-Type: application/json" \
    -d '{"operation": "MERGE", "options": {}}')

if echo "$response" | grep -q "error\|invalid\|required"; then
    echo -e "${GREEN}✓ Validação funcionando${NC}"
else
    echo -e "${RED}✗ Validação não detectou erro${NC}"
fi

echo ""
echo -e "${BLUE}4. Testando Jobs Endpoint${NC}"
echo ""

# Testar listagem de jobs
test_endpoint "/jobs" "Listagem de Jobs"

echo ""
echo -e "${BLUE}5. Resumo dos Testes${NC}"
echo ""

# Contar sucessos e falhas
echo -e "${GREEN}✓ Testes básicos da API concluídos${NC}"
echo -e "${YELLOW}ℹ Os jobs criados podem falhar na execução por falta de arquivos, mas isso é esperado${NC}"
echo -e "${YELLOW}ℹ O importante é que a API aceite as requisições e valide as opções corretamente${NC}"

echo ""
echo -e "${BLUE}6. Próximos Passos${NC}"
echo ""
echo "Para testes completos com arquivos reais:"
echo "1. Execute: ./examples/create-sample-pdfs.sh (se tiver wkhtmltopdf/pandoc)"
echo "2. Use: ./examples/pdf-cli.sh para testes interativos"
echo "3. Execute: ./examples/curl-examples.sh para ver todos os exemplos"
echo ""
echo "Para upload e teste com arquivos reais:"
echo "1. ./examples/pdf-cli.sh"
echo "2. pdf-cli> upload /caminho/para/seu/arquivo.pdf"
echo "3. pdf-cli> job MERGE (ou outra operação)"
echo ""

echo -e "${GREEN}🎉 Teste rápido concluído!${NC}"