#!/bin/bash

# Script para criar PDFs de exemplo para testes
# Requer: pandoc, wkhtmltopdf ou similar

SAMPLE_DIR="/home/otavio/API-JAVA/examples/sample-pdfs"

# Cores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Criando PDFs de Exemplo ===${NC}"

# Criar diretório
mkdir -p "$SAMPLE_DIR"

# Função para criar PDF usando HTML e wkhtmltopdf (se disponível)
create_pdf_from_html() {
    local filename=$1
    local title=$2
    local content=$3
    
    local html_file="$SAMPLE_DIR/${filename%.pdf}.html"
    local pdf_file="$SAMPLE_DIR/$filename"
    
    cat > "$html_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>$title</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
        h1 { color: #333; border-bottom: 2px solid #333; }
        h2 { color: #666; }
        .page-break { page-break-before: always; }
        .watermark { position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%) rotate(-45deg); 
                    font-size: 72px; color: rgba(200,200,200,0.3); z-index: -1; }
    </style>
</head>
<body>
    $content
</body>
</html>
EOF

    # Tentar criar PDF com wkhtmltopdf
    if command -v wkhtmltopdf >/dev/null 2>&1; then
        wkhtmltopdf --page-size A4 --margin-top 20mm --margin-bottom 20mm \
                    --margin-left 15mm --margin-right 15mm \
                    "$html_file" "$pdf_file" 2>/dev/null
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Criado: $filename${NC}"
            rm "$html_file"
            return 0
        fi
    fi
    
    # Tentar com pandoc
    if command -v pandoc >/dev/null 2>&1; then
        pandoc "$html_file" -o "$pdf_file" 2>/dev/null
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Criado: $filename (via pandoc)${NC}"
            rm "$html_file"
            return 0
        fi
    fi
    
    echo -e "${YELLOW}⚠ Não foi possível criar $filename - wkhtmltopdf ou pandoc necessários${NC}"
    rm "$html_file"
    return 1
}

# PDF 1: Documento simples
create_pdf_from_html "document1.pdf" "Documento de Teste 1" '
<h1>Documento de Teste 1</h1>
<h2>Introdução</h2>
<p>Este é um documento PDF de exemplo criado para testar as operações da API de processamento de PDF.</p>
<p>Este documento contém múltiplas páginas e diferentes tipos de conteúdo para facilitar os testes de merge, split, rotate e outras operações.</p>

<h2>Seção 1: Conteúdo de Exemplo</h2>
<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p>
<p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>

<div class="page-break"></div>
<h2>Página 2: Mais Conteúdo</h2>
<p>Esta é a segunda página do documento. Ela contém conteúdo adicional para testar operações como split e crop.</p>
<ul>
    <li>Item de lista 1</li>
    <li>Item de lista 2</li>
    <li>Item de lista 3</li>
    <li>Item de lista 4</li>
    <li>Item de lista 5</li>
</ul>

<h3>Tabela de Exemplo</h3>
<table border="1" style="border-collapse: collapse; width: 100%;">
    <tr><th>Coluna 1</th><th>Coluna 2</th><th>Coluna 3</th></tr>
    <tr><td>Dados 1</td><td>Dados 2</td><td>Dados 3</td></tr>
    <tr><td>Dados 4</td><td>Dados 5</td><td>Dados 6</td></tr>
    <tr><td>Dados 7</td><td>Dados 8</td><td>Dados 9</td></tr>
</table>

<div class="page-break"></div>
<h2>Página 3: Página Final</h2>
<p>Esta é a terceira e última página do documento de teste.</p>
<p>Ideal para testar operações de reordenação e extração de páginas específicas.</p>
<p><strong>Data de criação:</strong> $(date)</p>
<p><strong>Propósito:</strong> Teste da API de Processamento de PDF</p>'

# PDF 2: Documento para merge
create_pdf_from_html "document2.pdf" "Documento de Teste 2" '
<h1>Documento de Teste 2</h1>
<h2>Documento para Merge</h2>
<p>Este é o segundo documento PDF criado especificamente para testar operações de merge (combinação) de PDFs.</p>

<h2>Características deste documento:</h2>
<ul>
    <li>Formatação diferente do documento 1</li>
    <li>Conteúdo complementar</li>
    <li>Ideal para testes de merge</li>
    <li>Contém 2 páginas</li>
</ul>

<h2>Seção Especial</h2>
<p>Este conteúdo será combinado com o documento 1 durante os testes de merge.</p>
<p>A API deve ser capaz de combinar este documento com outros mantendo a formatação e estrutura.</p>

<div class="page-break"></div>
<h2>Segunda Página do Documento 2</h2>
<p>Esta página será a última no PDF resultante do merge com document1.pdf.</p>
<p>Conteúdo adicional para verificar se o merge preserva todas as páginas corretamente.</p>
<p><em>Fim do documento 2</em></p>'

# PDF 3: Documento com múltiplas páginas para split
create_pdf_from_html "multi-page.pdf" "Documento Multi-Página" '
<h1>Documento Multi-Página</h1>
<p>Este documento contém 5 páginas para testar operações de split.</p>
<p><strong>Página atual: 1</strong></p>

<div class="page-break"></div>
<h1>Página 2</h1>
<p>Segunda página do documento multi-página.</p>
<p>Conteúdo específico da página 2 para identificação após split.</p>
<p><strong>Página atual: 2</strong></p>

<div class="page-break"></div>
<h1>Página 3</h1>
<p>Terceira página do documento multi-página.</p>
<p>Esta página pode ser extraída individualmente usando split.</p>
<p><strong>Página atual: 3</strong></p>

<div class="page-break"></div>
<h1>Página 4</h1>
<p>Quarta página do documento multi-página.</p>
<p>Ideal para testar intervalos de páginas no split.</p>
<p><strong>Página atual: 4</strong></p>

<div class="page-break"></div>
<h1>Página 5</h1>
<p>Quinta e última página do documento multi-página.</p>
<p>Página final para completar os testes de split.</p>
<p><strong>Página atual: 5</strong></p>'

# PDF 4: Documento para watermark
create_pdf_from_html "watermark-test.pdf" "Documento para Watermark" '
<h1>Documento para Teste de Watermark</h1>
<p>Este documento foi criado especificamente para testar a funcionalidade de watermark (marca d\'água).</p>

<h2>Características:</h2>
<ul>
    <li>Fundo claro para melhor visibilidade do watermark</li>
    <li>Texto distribuído pela página</li>
    <li>Espaços em branco para o watermark</li>
</ul>

<p>O watermark deve aparecer sobre este conteúdo de forma semi-transparente.</p>

<div style="height: 200px;"></div>

<p>Mais conteúdo na parte inferior da página para verificar se o watermark cobre toda a área.</p>'

# PDF 5: Documento para crop
create_pdf_from_html "crop-test.pdf" "Documento para Crop" '
<div style="border: 2px solid red; padding: 20px; margin: 50px;">
    <h1>Área para Crop</h1>
    <p>Este conteúdo está dentro de uma área delimitada para facilitar o teste de crop.</p>
    <p>As coordenadas desta área são aproximadamente:</p>
    <ul>
        <li>X: 50px</li>
        <li>Y: 50px</li>
        <li>Width: 400px</li>
        <li>Height: 300px</li>
    </ul>
</div>

<p>Este conteúdo está fora da área de crop e deve ser removido na operação.</p>
<p>Conteúdo adicional que não deve aparecer no PDF cortado.</p>'

echo ""
echo -e "${BLUE}=== Resumo dos Arquivos Criados ===${NC}"
echo -e "${YELLOW}Diretório: $SAMPLE_DIR${NC}"
echo ""

if [ -d "$SAMPLE_DIR" ]; then
    ls -la "$SAMPLE_DIR"/*.pdf 2>/dev/null | while read -r line; do
        echo "  $line"
    done
fi

echo ""
echo -e "${BLUE}=== Como usar os arquivos de exemplo ===${NC}"
echo ""
echo "1. Upload dos arquivos:"
echo "   ./pdf-cli.sh upload $SAMPLE_DIR/document1.pdf"
echo "   ./pdf-cli.sh upload $SAMPLE_DIR/document2.pdf"
echo ""
echo "2. Teste de merge:"
echo "   ./pdf-cli.sh quick-merge"
echo ""
echo "3. Teste de split:"
echo "   ./pdf-cli.sh upload $SAMPLE_DIR/multi-page.pdf"
echo "   ./pdf-cli.sh job SPLIT"
echo ""
echo "4. Teste de watermark:"
echo "   ./pdf-cli.sh upload $SAMPLE_DIR/watermark-test.pdf"
echo "   ./pdf-cli.sh job WATERMARK"
echo ""
echo "5. Teste de crop:"
echo "   ./pdf-cli.sh upload $SAMPLE_DIR/crop-test.pdf"
echo "   ./pdf-cli.sh job PDF_CROP"
echo ""

if ! command -v wkhtmltopdf >/dev/null 2>&1 && ! command -v pandoc >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠ Para criar os PDFs automaticamente, instale:${NC}"
    echo "   sudo apt-get install wkhtmltopdf  # ou"
    echo "   sudo apt-get install pandoc texlive-latex-recommended"
    echo ""
    echo -e "${YELLOW}Alternativamente, você pode usar qualquer PDF existente para os testes.${NC}"
fi