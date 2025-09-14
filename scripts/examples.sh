#!/bin/bash

# Script com exemplos de uso da PDF Processor API
# Demonstra como usar diferentes operações via curl

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
API_KEY="${API_KEY:-dev123}"

echo "📚 Exemplos de uso da PDF Processor API"
echo "======================================"
echo ""

# Verificar se a API está rodando
log_info "Verificando se a API está disponível..."
if ! curl -s -f -H "X-API-Key: $API_KEY" "$API_BASE/../actuator/health" > /dev/null; then
    log_error "API não está disponível em $API_BASE"
    echo "Execute primeiro: ./scripts/run-api.sh"
    exit 1
fi
log_success "API está disponível"

echo ""
echo "🔧 Configurações:"
echo "  API Base: $API_BASE"
echo "  API Key: $API_KEY"
echo ""

# Função para executar exemplo
run_example() {
    local title="$1"
    local description="$2"
    local command="$3"
    
    echo "📋 $title"
    echo "   $description"
    echo ""
    echo "   Comando:"
    echo "   $command"
    echo ""
    
    read -p "   Executar este exemplo? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "   Executando..."
        eval "$command"
        echo ""
        log_success "Exemplo concluído"
    else
        log_info "Exemplo pulado"
    fi
    echo ""
    echo "----------------------------------------"
    echo ""
}

# Exemplo 1: Health Check
run_example "Health Check" \
    "Verificar status da API" \
    "curl -H \"X-API-Key: $API_KEY\" \"$API_BASE/../actuator/health\" | jq ."

# Exemplo 2: Listar Jobs
run_example "Listar Jobs" \
    "Listar todos os jobs existentes" \
    "curl -H \"X-API-Key: $API_KEY\" \"$API_BASE/jobs\" | jq ."

# Exemplo 3: Criar PDF simples
run_example "Criar PDF" \
    "Criar um PDF simples com texto" \
    "curl -X POST -H \"X-API-Key: $API_KEY\" \\
    -F \"operation=pdf_create\" \\
    -F 'optionsJson={\"text_content\":\"Hello World! Este é um PDF criado pela API.\",\"title\":\"Documento de Teste\",\"author\":\"PDF Processor API\"}' \\
    \"$API_BASE/jobs\" | jq ."

# Exemplo 4: Merge PDFs (requer arquivos)
echo "📋 Merge de PDFs"
echo "   Combinar múltiplos PDFs em um único arquivo"
echo ""
echo "   ⚠️  Este exemplo requer arquivos PDF. Certifique-se de ter PDFs na pasta examples/"
echo ""
read -p "   Você tem arquivos PDF para fazer merge? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "   Exemplo de comando:"
    echo "   curl -X POST -H \"X-API-Key: $API_KEY\" \\"
    echo "     -F \"operation=merge\" \\"
    echo "     -F \"files=@examples/sample-pdfs/sample.pdf\" \\"
    echo "     -F \"files=@examples/sample-pdfs/test1.pdf\" \\"
    echo "     -F 'optionsJson={\"output_filename\":\"merged_document.pdf\"}' \\"
    echo "     \"$API_BASE/jobs\""
else
    log_info "Exemplo de merge pulado"
fi
echo ""
echo "----------------------------------------"
echo ""

# Exemplo 5: Watermark
run_example "Adicionar Watermark" \
    "Adicionar marca d'água a um PDF (requer arquivo)" \
    "echo 'Exemplo de comando para watermark:
curl -X POST -H \"X-API-Key: $API_KEY\" \\\\
  -F \"operation=watermark\" \\\\
  -F \"files=@examples/sample-pdfs/sample.pdf\" \\\\
  -F '\"optionsJson={\\\"text\\\":\\\"CONFIDENCIAL\\\",\\\"opacity\\\":0.5,\\\"position\\\":\\\"center\\\"}\"' \\\\
  \"$API_BASE/jobs\"'"

# Exemplo 6: Verificar status de job
echo "📋 Verificar Status de Job"
echo "   Verificar o progresso de um job específico"
echo ""
echo "   Exemplo de comando:"
echo "   curl -H \"X-API-Key: $API_KEY\" \"$API_BASE/jobs/{JOB_ID}\" | jq ."
echo ""
echo "   Substitua {JOB_ID} pelo ID retornado ao criar um job"
echo ""
echo "----------------------------------------"
echo ""

# Exemplo 7: Download de resultado
echo "📋 Download de Resultado"
echo "   Fazer download do arquivo processado"
echo ""
echo "   Exemplo de comando:"
echo "   curl -H \"X-API-Key: $API_KEY\" -O -J \"$API_BASE/jobs/{JOB_ID}/download\""
echo ""
echo "   Substitua {JOB_ID} pelo ID de um job concluído"
echo ""
echo "----------------------------------------"
echo ""

# Exemplo 8: Server-Sent Events
echo "📋 Monitorar Progresso em Tempo Real (SSE)"
echo "   Acompanhar progresso de um job via Server-Sent Events"
echo ""
echo "   Exemplo de comando:"
echo "   curl -H \"X-API-Key: $API_KEY\" -H \"Accept: text/event-stream\" \"$API_BASE/jobs/{JOB_ID}/events\""
echo ""
echo "   Substitua {JOB_ID} pelo ID de um job em execução"
echo ""
echo "----------------------------------------"
echo ""

# Operações avançadas
echo "🚀 Operações Avançadas Disponíveis:"
echo ""
echo "   • PDF_CROP - Recortar páginas"
echo "   • PDF_REORDER - Reordenar páginas"
echo "   • PDF_RESIZE - Redimensionar páginas"
echo "   • PDF_COMPARE - Comparar dois PDFs"
echo "   • PDF_OPTIMIZE - Otimizar PDF"
echo "   • PDF_VALIDATE - Validar PDF"
echo "   • PDF_REPAIR - Reparar PDF corrompido"
echo "   • PDF_TO_IMAGES - Converter para imagens"
echo "   • IMAGES_TO_PDF - Criar PDF de imagens"
echo "   • PDF_OCR - Aplicar OCR"
echo "   • PDF_TO_AUDIO - Converter para áudio"
echo "   • PDF_EDIT - Editar conteúdo"
echo "   • PDF_PROTECT - Proteger com senha"
echo "   • PDF_UNLOCK - Remover proteção"
echo "   • PDF_EXTRACT_RESOURCES - Extrair recursos"
echo "   • PDF_REMOVE_RESOURCES - Remover recursos"
echo ""

echo "📖 Documentação Completa:"
echo "   Swagger UI: http://localhost:8080/swagger-ui"
echo "   OpenAPI Spec: http://localhost:8080/v3/api-docs"
echo ""

log_success "🎉 Exemplos concluídos!"
echo ""
echo "Para mais informações, consulte:"
echo "  • README.md"
echo "  • examples/curl-examples.sh"
echo "  • Documentação OpenAPI"
