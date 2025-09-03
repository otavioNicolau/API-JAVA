#!/bin/bash

# PDF Processing API - CLI de Desenvolvimento
# Script interativo para facilitar o desenvolvimento e teste da API

API_BASE="http://localhost:8080/api/v1"
TEMP_DIR="/tmp/pdf-cli"
CONFIG_FILE="$HOME/.pdf-cli-config"

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Criar diretório temporário
mkdir -p "$TEMP_DIR"

# Função para carregar configuração
load_config() {
    if [ -f "$CONFIG_FILE" ]; then
        source "$CONFIG_FILE"
    fi
}

# Função para salvar configuração
save_config() {
    cat > "$CONFIG_FILE" << EOF
API_BASE="$API_BASE"
LAST_JOB_ID="$LAST_JOB_ID"
LAST_FILE_ID="$LAST_FILE_ID"
EOF
}

# Função para mostrar ajuda
show_help() {
    echo -e "${BLUE}=== PDF Processing CLI - Ajuda ===${NC}"
    echo ""
    echo -e "${YELLOW}Comandos disponíveis:${NC}"
    echo "  help, h              - Mostrar esta ajuda"
    echo "  status, s            - Verificar status da API"
    echo "  operations, ops      - Listar operações suportadas"
    echo "  schema <operation>   - Mostrar esquema de opções"
    echo "  upload <file>        - Fazer upload de arquivo"
    echo "  download <file-id>   - Baixar arquivo"
    echo "  job <operation>      - Criar job interativo"
    echo "  jobs                 - Listar jobs"
    echo "  job-status <job-id>  - Verificar status do job"
    echo "  quick-merge          - Merge rápido de 2 PDFs"
    echo "  quick-split          - Split rápido de PDF"
    echo "  quick-watermark      - Watermark rápido"
    echo "  quick-crop           - Crop rápido"
    echo "  examples             - Mostrar exemplos de uso"
    echo "  config               - Configurar CLI"
    echo "  clean                - Limpar arquivos temporários"
    echo "  exit, quit, q        - Sair"
    echo ""
}

# Função para verificar API
check_api() {
    if curl -s "$API_BASE/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✓ API está rodando em $API_BASE${NC}"
        return 0
    else
        echo -e "${RED}✗ API não está rodando em $API_BASE${NC}"
        return 1
    fi
}

# Função para listar operações
list_operations() {
    echo -e "${BLUE}=== Operações Suportadas ===${NC}"
    local response=$(curl -s "$API_BASE/operations")
    if [ $? -eq 0 ]; then
        echo "$response" | jq -r '.[] | "  - " + .'
    else
        echo -e "${RED}Erro ao obter operações${NC}"
    fi
    echo ""
}

# Função para mostrar esquema
show_schema() {
    local operation=$1
    if [ -z "$operation" ]; then
        echo -e "${RED}Uso: schema <operation>${NC}"
        return 1
    fi
    
    echo -e "${BLUE}=== Esquema para $operation ===${NC}"
    local response=$(curl -s "$API_BASE/operations/$operation/options-schema")
    if [ $? -eq 0 ]; then
        echo "$response" | jq .
    else
        echo -e "${RED}Erro ao obter esquema para $operation${NC}"
    fi
    echo ""
}

# Função para upload
upload_file() {
    local file_path=$1
    if [ -z "$file_path" ]; then
        echo -e "${YELLOW}Digite o caminho do arquivo:${NC}"
        read -r file_path
    fi
    
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}Arquivo não encontrado: $file_path${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Fazendo upload de $file_path...${NC}"
    local response=$(curl -s -X POST "$API_BASE/files/upload" -F "file=@$file_path")
    
    if [ $? -eq 0 ]; then
        local file_id=$(echo "$response" | jq -r '.id')
        echo -e "${GREEN}✓ Upload realizado com sucesso${NC}"
        echo -e "File ID: ${YELLOW}$file_id${NC}"
        LAST_FILE_ID="$file_id"
        save_config
    else
        echo -e "${RED}Erro no upload${NC}"
    fi
    echo ""
}

# Função para download
download_file() {
    local file_id=$1
    if [ -z "$file_id" ]; then
        if [ -n "$LAST_FILE_ID" ]; then
            echo -e "${YELLOW}Usar último file ID ($LAST_FILE_ID)? [Y/n]:${NC}"
            read -r use_last
            if [ "$use_last" != "n" ] && [ "$use_last" != "N" ]; then
                file_id="$LAST_FILE_ID"
            fi
        fi
        
        if [ -z "$file_id" ]; then
            echo -e "${YELLOW}Digite o File ID:${NC}"
            read -r file_id
        fi
    fi
    
    if [ -z "$file_id" ]; then
        echo -e "${RED}File ID é obrigatório${NC}"
        return 1
    fi
    
    local output_file="$TEMP_DIR/download_$(date +%s).pdf"
    echo -e "${BLUE}Baixando arquivo $file_id...${NC}"
    
    if curl -s "$API_BASE/files/$file_id/download" -o "$output_file"; then
        echo -e "${GREEN}✓ Arquivo baixado: $output_file${NC}"
    else
        echo -e "${RED}Erro ao baixar arquivo${NC}"
    fi
    echo ""
}

# Função para criar job interativo
create_job_interactive() {
    local operation=$1
    if [ -z "$operation" ]; then
        echo -e "${YELLOW}Operações disponíveis:${NC}"
        curl -s "$API_BASE/operations" | jq -r '.[] | "  - " + .'
        echo -e "${YELLOW}Digite a operação:${NC}"
        read -r operation
    fi
    
    if [ -z "$operation" ]; then
        echo -e "${RED}Operação é obrigatória${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Criando job para operação: $operation${NC}"
    echo -e "${YELLOW}Esquema de opções:${NC}"
    curl -s "$API_BASE/operations/$operation/options-schema" | jq .
    
    echo -e "${YELLOW}Digite as opções em JSON (ou pressione Enter para exemplo):${NC}"
    read -r options_json
    
    if [ -z "$options_json" ]; then
        case "$operation" in
            "MERGE")
                options_json='{"inputFiles": ["file1.pdf", "file2.pdf"], "outputFileName": "merged.pdf"}'
                ;;
            "SPLIT")
                options_json='{"inputFile": "document.pdf", "pages": "1-3", "outputPrefix": "page_"}'
                ;;
            "ROTATE")
                options_json='{"inputFile": "document.pdf", "rotation": 90, "pages": "all", "outputFileName": "rotated.pdf"}'
                ;;
            "WATERMARK")
                options_json='{"inputFile": "document.pdf", "text": "SAMPLE", "fontSize": 36, "opacity": 0.5, "position": "center", "outputFileName": "watermarked.pdf"}'
                ;;
            "PDF_CROP")
                options_json='{"inputFile": "document.pdf", "x": 50, "y": 50, "width": 400, "height": 600, "pages": "all", "outputFileName": "cropped.pdf"}'
                ;;
            *)
                echo -e "${RED}Exemplo não disponível para $operation${NC}"
                return 1
                ;;
        esac
        echo -e "${CYAN}Usando exemplo: $options_json${NC}"
    fi
    
    local payload="{\"operation\": \"$operation\", \"options\": $options_json}"
    
    echo -e "${BLUE}Enviando job...${NC}"
    local response=$(curl -s -X POST "$API_BASE/jobs" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    if [ $? -eq 0 ]; then
        local job_id=$(echo "$response" | jq -r '.id')
        echo -e "${GREEN}✓ Job criado com sucesso${NC}"
        echo -e "Job ID: ${YELLOW}$job_id${NC}"
        LAST_JOB_ID="$job_id"
        save_config
        
        echo -e "${YELLOW}Verificar status automaticamente? [Y/n]:${NC}"
        read -r check_status
        if [ "$check_status" != "n" ] && [ "$check_status" != "N" ]; then
            sleep 2
            check_job_status "$job_id"
        fi
    else
        echo -e "${RED}Erro ao criar job${NC}"
        echo "Response: $response"
    fi
    echo ""
}

# Função para verificar status do job
check_job_status() {
    local job_id=$1
    if [ -z "$job_id" ]; then
        if [ -n "$LAST_JOB_ID" ]; then
            echo -e "${YELLOW}Usar último job ID ($LAST_JOB_ID)? [Y/n]:${NC}"
            read -r use_last
            if [ "$use_last" != "n" ] && [ "$use_last" != "N" ]; then
                job_id="$LAST_JOB_ID"
            fi
        fi
        
        if [ -z "$job_id" ]; then
            echo -e "${YELLOW}Digite o Job ID:${NC}"
            read -r job_id
        fi
    fi
    
    if [ -z "$job_id" ]; then
        echo -e "${RED}Job ID é obrigatório${NC}"
        return 1
    fi
    
    echo -e "${BLUE}Verificando status do job: $job_id${NC}"
    local response=$(curl -s "$API_BASE/jobs/$job_id")
    
    if [ $? -eq 0 ]; then
        echo "$response" | jq .
        local status=$(echo "$response" | jq -r '.status')
        local result_file=$(echo "$response" | jq -r '.resultFileId // empty')
        
        case "$status" in
            "COMPLETED")
                echo -e "${GREEN}✓ Job concluído com sucesso${NC}"
                if [ -n "$result_file" ] && [ "$result_file" != "null" ]; then
                    echo -e "Arquivo resultado: ${YELLOW}$result_file${NC}"
                    echo -e "${YELLOW}Baixar arquivo resultado? [Y/n]:${NC}"
                    read -r download_result
                    if [ "$download_result" != "n" ] && [ "$download_result" != "N" ]; then
                        download_file "$result_file"
                    fi
                fi
                ;;
            "FAILED")
                echo -e "${RED}✗ Job falhou${NC}"
                ;;
            "PROCESSING")
                echo -e "${YELLOW}⏳ Job em processamento${NC}"
                ;;
            "PENDING")
                echo -e "${CYAN}⏸ Job pendente${NC}"
                ;;
        esac
    else
        echo -e "${RED}Erro ao verificar status do job${NC}"
    fi
    echo ""
}

# Função para listar jobs
list_jobs() {
    echo -e "${BLUE}=== Jobs Recentes ===${NC}"
    local response=$(curl -s "$API_BASE/jobs")
    if [ $? -eq 0 ]; then
        echo "$response" | jq .
    else
        echo -e "${RED}Erro ao listar jobs${NC}"
    fi
    echo ""
}

# Funções quick para operações comuns
quick_merge() {
    echo -e "${BLUE}=== Merge Rápido ===${NC}"
    echo -e "${YELLOW}Digite o primeiro arquivo PDF:${NC}"
    read -r file1
    echo -e "${YELLOW}Digite o segundo arquivo PDF:${NC}"
    read -r file2
    echo -e "${YELLOW}Nome do arquivo de saída (opcional):${NC}"
    read -r output
    
    if [ -z "$output" ]; then
        output="merged_$(date +%s).pdf"
    fi
    
    local options="{\"inputFiles\": [\"$file1\", \"$file2\"], \"outputFileName\": \"$output\"}"
    local payload="{\"operation\": \"MERGE\", \"options\": $options}"
    
    echo -e "${BLUE}Criando job de merge...${NC}"
    local response=$(curl -s -X POST "$API_BASE/jobs" \
        -H "Content-Type: application/json" \
        -d "$payload")
    
    local job_id=$(echo "$response" | jq -r '.id')
    echo -e "${GREEN}Job criado: $job_id${NC}"
    LAST_JOB_ID="$job_id"
    save_config
}

# Função para configurar CLI
configure_cli() {
    echo -e "${BLUE}=== Configuração do CLI ===${NC}"
    echo -e "${YELLOW}URL base atual: $API_BASE${NC}"
    echo -e "${YELLOW}Digite nova URL base (ou Enter para manter):${NC}"
    read -r new_base
    
    if [ -n "$new_base" ]; then
        API_BASE="$new_base"
        save_config
        echo -e "${GREEN}✓ URL base atualizada para: $API_BASE${NC}"
    fi
    echo ""
}

# Função para limpar arquivos temporários
clean_temp() {
    echo -e "${BLUE}Limpando arquivos temporários...${NC}"
    rm -rf "$TEMP_DIR"/*
    echo -e "${GREEN}✓ Arquivos temporários removidos${NC}"
    echo ""
}

# Função para mostrar exemplos
show_examples() {
    echo -e "${BLUE}=== Exemplos de Uso ===${NC}"
    echo ""
    echo -e "${YELLOW}1. Upload e Merge:${NC}"
    echo "   upload documento1.pdf"
    echo "   upload documento2.pdf"
    echo "   quick-merge"
    echo ""
    echo -e "${YELLOW}2. Watermark:${NC}"
    echo "   job WATERMARK"
    echo "   # Digite: {\"inputFile\": \"doc.pdf\", \"text\": \"CONFIDENCIAL\", \"fontSize\": 48}"
    echo ""
    echo -e "${YELLOW}3. Crop avançado:${NC}"
    echo "   job PDF_CROP"
    echo "   # Digite: {\"inputFile\": \"doc.pdf\", \"x\": 50, \"y\": 50, \"width\": 400, \"height\": 600}"
    echo ""
}

# Função principal interativa
interactive_mode() {
    echo -e "${PURPLE}=== PDF Processing CLI - Modo Interativo ===${NC}"
    echo -e "${CYAN}Digite 'help' para ver comandos disponíveis${NC}"
    echo ""
    
    while true; do
        echo -ne "${GREEN}pdf-cli> ${NC}"
        read -r command args
        
        case "$command" in
            "help"|"h")
                show_help
                ;;
            "status"|"s")
                check_api
                ;;
            "operations"|"ops")
                list_operations
                ;;
            "schema")
                show_schema "$args"
                ;;
            "upload")
                upload_file "$args"
                ;;
            "download")
                download_file "$args"
                ;;
            "job")
                create_job_interactive "$args"
                ;;
            "jobs")
                list_jobs
                ;;
            "job-status")
                check_job_status "$args"
                ;;
            "quick-merge")
                quick_merge
                ;;
            "examples")
                show_examples
                ;;
            "config")
                configure_cli
                ;;
            "clean")
                clean_temp
                ;;
            "exit"|"quit"|"q")
                echo -e "${BLUE}Até logo!${NC}"
                break
                ;;
            "")
                # Comando vazio, continuar
                ;;
            *)
                echo -e "${RED}Comando desconhecido: $command${NC}"
                echo -e "${YELLOW}Digite 'help' para ver comandos disponíveis${NC}"
                ;;
        esac
        echo ""
    done
}

# Função principal
main() {
    load_config
    
    if [ $# -eq 0 ]; then
        interactive_mode
    else
        case "$1" in
            "help"|"h")
                show_help
                ;;
            "status"|"s")
                check_api
                ;;
            "operations"|"ops")
                list_operations
                ;;
            "schema")
                show_schema "$2"
                ;;
            "upload")
                upload_file "$2"
                ;;
            "download")
                download_file "$2"
                ;;
            "job")
                create_job_interactive "$2"
                ;;
            "jobs")
                list_jobs
                ;;
            "job-status")
                check_job_status "$2"
                ;;
            "examples")
                show_examples
                ;;
            "config")
                configure_cli
                ;;
            "clean")
                clean_temp
                ;;
            *)
                echo -e "${RED}Comando desconhecido: $1${NC}"
                show_help
                ;;
        esac
    fi
}

# Executar
main "$@"