#!/bin/bash

# PDF Processing API - Exemplos de uso com curl
# Execute este script para testar todas as operações da API

API_BASE="http://localhost:8080/api/v1"

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== PDF Processing API - Exemplos de Uso ===${NC}"
echo -e "${YELLOW}Base URL: $API_BASE${NC}"
echo ""

# Função para verificar se a API está rodando
check_api() {
    echo -e "${BLUE}Verificando se a API está rodando...${NC}"
    if curl -s "$API_BASE/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API está rodando${NC}"
    else
        echo -e "${RED}✗ API não está rodando. Execute 'mvn spring-boot:run -pl app/api' primeiro${NC}"
        exit 1
    fi
    echo ""
}

# Função para criar um job
create_job() {
    local operation=$1
    local options=$2
    local description=$3
    
    echo -e "${YELLOW}Criando job: $description${NC}"
    
    local payload="{
        \"operation\": \"$operation\",
        \"options\": $options
    }"
    
    echo "Payload: $payload"
    
    local response=$(curl -s -X POST "$API_BASE/jobs" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    echo "Response: $response"
    
    local job_id=$(echo $response | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    
    if [ -n "$job_id" ]; then
        echo -e "${GREEN}✓ Job criado com ID: $job_id${NC}"
        echo "$job_id"
    else
        echo -e "${RED}✗ Erro ao criar job${NC}"
        return 1
    fi
    echo ""
}

# Função para verificar status do job
check_job_status() {
    local job_id=$1
    
    echo -e "${BLUE}Verificando status do job: $job_id${NC}"
    
    local response=$(curl -s "$API_BASE/jobs/$job_id")
    echo "Response: $response"
    
    local status=$(echo $response | grep -o '"status":"[^"]*' | cut -d'"' -f4)
    echo -e "Status: ${YELLOW}$status${NC}"
    echo ""
}

# Função para fazer upload de arquivo
upload_file() {
    local file_path=$1
    local description=$2
    
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}✗ Arquivo não encontrado: $file_path${NC}"
        return 1
    fi
    
    echo -e "${YELLOW}Fazendo upload: $description${NC}"
    echo "Arquivo: $file_path"
    
    local response=$(curl -s -X POST "$API_BASE/files/upload" \
        -F "file=@$file_path")
    
    echo "Response: $response"
    
    local file_id=$(echo $response | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    
    if [ -n "$file_id" ]; then
        echo -e "${GREEN}✓ Upload realizado com ID: $file_id${NC}"
        echo "$file_id"
    else
        echo -e "${RED}✗ Erro no upload${NC}"
        return 1
    fi
    echo ""
}

# Função para baixar arquivo
download_file() {
    local file_id=$1
    local output_path=$2
    local description=$3
    
    echo -e "${YELLOW}Baixando arquivo: $description${NC}"
    echo "File ID: $file_id"
    echo "Output: $output_path"
    
    if curl -s "$API_BASE/files/$file_id/download" -o "$output_path"; then
        echo -e "${GREEN}✓ Arquivo baixado: $output_path${NC}"
    else
        echo -e "${RED}✗ Erro ao baixar arquivo${NC}"
        return 1
    fi
    echo ""
}

# Função para listar operações suportadas
list_operations() {
    echo -e "${BLUE}=== Operações Suportadas ===${NC}"
    
    local response=$(curl -s "$API_BASE/operations")
    echo "$response" | jq -r '.[] | "- " + .'
    echo ""
}

# Função para obter esquema de opções
get_options_schema() {
    local operation=$1
    
    echo -e "${BLUE}=== Esquema de Opções para $operation ===${NC}"
    
    local response=$(curl -s "$API_BASE/operations/$operation/options-schema")
    echo "$response" | jq .
    echo ""
}

# Função principal
main() {
    check_api
    
    echo -e "${BLUE}=== Exemplos de Uso ===${NC}"
    echo ""
    
    # Listar operações
    list_operations
    
    # Exemplos de esquemas de opções
    echo -e "${BLUE}=== Exemplos de Esquemas de Opções ===${NC}"
    get_options_schema "MERGE"
    get_options_schema "PDF_CROP"
    get_options_schema "WATERMARK"
    
    echo -e "${BLUE}=== Exemplos de Jobs ===${NC}"
    
    # Exemplo 1: Merge de PDFs
    echo -e "${GREEN}Exemplo 1: Merge de PDFs${NC}"
    local merge_options='{
        "inputFiles": ["file1.pdf", "file2.pdf"],
        "outputFileName": "merged.pdf"
    }'
    create_job "MERGE" "$merge_options" "Merge de dois PDFs"
    
    # Exemplo 2: Split de PDF
    echo -e "${GREEN}Exemplo 2: Split de PDF${NC}"
    local split_options='{
        "inputFile": "document.pdf",
        "pages": "1-3,5,7-10",
        "outputPrefix": "page_"
    }'
    create_job "SPLIT" "$split_options" "Split de páginas específicas"
    
    # Exemplo 3: Rotação de PDF
    echo -e "${GREEN}Exemplo 3: Rotação de PDF${NC}"
    local rotate_options='{
        "inputFile": "document.pdf",
        "rotation": 90,
        "pages": "all",
        "outputFileName": "rotated.pdf"
    }'
    create_job "ROTATE" "$rotate_options" "Rotação de 90 graus"
    
    # Exemplo 4: Watermark
    echo -e "${GREEN}Exemplo 4: Adicionar Watermark${NC}"
    local watermark_options='{
        "inputFile": "document.pdf",
        "text": "CONFIDENCIAL",
        "fontSize": 48,
        "opacity": 0.3,
        "position": "center",
        "color": "red",
        "outputFileName": "watermarked.pdf"
    }'
    create_job "WATERMARK" "$watermark_options" "Watermark de texto"
    
    # Exemplo 5: Criptografia
    echo -e "${GREEN}Exemplo 5: Criptografar PDF${NC}"
    local encrypt_options='{
        "inputFile": "document.pdf",
        "userPassword": "user123",
        "ownerPassword": "owner456",
        "permissions": ["PRINT", "COPY"],
        "outputFileName": "encrypted.pdf"
    }'
    create_job "ENCRYPT" "$encrypt_options" "Criptografia com senhas"
    
    # Exemplo 6: Crop de PDF (Operação Avançada)
    echo -e "${GREEN}Exemplo 6: Crop de PDF${NC}"
    local crop_options='{
        "inputFile": "document.pdf",
        "x": 50,
        "y": 50,
        "width": 400,
        "height": 600,
        "pages": "1-5",
        "outputFileName": "cropped.pdf"
    }'
    create_job "PDF_CROP" "$crop_options" "Crop de área específica"
    
    # Exemplo 7: Reordenar páginas (Operação Avançada)
    echo -e "${GREEN}Exemplo 7: Reordenar Páginas${NC}"
    local reorder_options='{
        "inputFile": "document.pdf",
        "pageOrder": [3, 1, 4, 2, 5],
        "outputFileName": "reordered.pdf"
    }'
    create_job "PDF_REORDER" "$reorder_options" "Reordenação de páginas"
    
    # Exemplo 8: Redimensionar PDF (Operação Avançada)
    echo -e "${GREEN}Exemplo 8: Redimensionar PDF${NC}"
    local resize_options='{
        "inputFile": "document.pdf",
        "pageSize": "A4",
        "scaleMode": "fit",
        "outputFileName": "resized.pdf"
    }'
    create_job "PDF_RESIZE" "$resize_options" "Redimensionamento para A4"
    
    # Exemplo 9: Compressão (Operação Avançada)
    echo -e "${GREEN}Exemplo 9: Compressão de PDF${NC}"
    local compress_options='{
        "inputFile": "document.pdf",
        "compressionLevel": "medium",
        "optimizeImages": true,
        "outputFileName": "compressed.pdf"
    }'
    create_job "COMPRESS" "$compress_options" "Compressão média"
    
    # Exemplo 10: Comparação de PDFs (Operação Avançada)
    echo -e "${GREEN}Exemplo 10: Comparação de PDFs${NC}"
    local compare_options='{
        "inputFiles": ["document1.pdf", "document2.pdf"],
        "outputFileName": "comparison_report.pdf",
        "compareText": true,
        "comparePages": true
    }'
    create_job "PDF_COMPARE" "$compare_options" "Comparação de dois PDFs"
    
    # Exemplo 11: Criação de PDF (Operação Avançada)
    echo -e "${GREEN}Exemplo 11: Criação de PDF${NC}"
    local create_options='{
        "pageSize": "A4",
        "content": "Este é um PDF criado automaticamente pela API.\n\nConteúdo de exemplo com múltiplas linhas.",
        "outputFileName": "new_document.pdf"
    }'
    create_job "PDF_CREATE" "$create_options" "Criação de novo PDF"
    
    echo -e "${BLUE}=== Fim dos Exemplos ===${NC}"
    echo -e "${YELLOW}Para usar este script:${NC}"
    echo "1. Certifique-se de que a API está rodando"
    echo "2. Execute: ./curl-examples.sh"
    echo "3. Para exemplos específicos, modifique as funções acima"
    echo ""
    echo -e "${YELLOW}Comandos úteis:${NC}"
    echo "- Listar jobs: curl $API_BASE/jobs"
    echo "- Status do job: curl $API_BASE/jobs/{job-id}"
    echo "- Download: curl $API_BASE/files/{file-id}/download -o output.pdf"
    echo "- Upload: curl -X POST $API_BASE/files/upload -F 'file=@input.pdf'"
}

# Executar se chamado diretamente
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    main "$@"
fi