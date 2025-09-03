# Teste das Operações Avançadas de PDF - ETAPA 12

Este documento demonstra as novas operações avançadas de PDF implementadas na ETAPA 12.

## Operações Implementadas

### 1. PDF_CROP - Recortar páginas do PDF
```bash
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "operation": "PDF_CROP",
    "options": {
      "input_file": "/path/to/input.pdf",
      "x": 50,
      "y": 50,
      "width": 500,
      "height": 700,
      "pages": "1-3"
    }
  }'
```

### 2. PDF_REORDER - Reordenar páginas do PDF
```bash
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "operation": "PDF_REORDER",
    "options": {
      "input_file": "/path/to/input.pdf",
      "page_order": "3,1,4,2"
    }
  }'
```

### 3. PDF_RESIZE - Redimensionar páginas do PDF
```bash
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "operation": "PDF_RESIZE",
    "options": {
      "input_file": "/path/to/input.pdf",
      "page_size": "A3",
      "pages": "all"
    }
  }'
```

### 4. COMPRESS - Comprimir PDF
```bash
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{
    "operation": "COMPRESS",
    "options": {
      "input_file": "/path/to/input.pdf",
      "quality": "medium"
    }
  }'
```

## Operações Não Implementadas (Requerem Dependências Adicionais)

### PDF_TO_IMAGES e IMAGES_TO_PDF
Estas operações requerem bibliotecas adicionais para processamento de imagens e foram marcadas como não suportadas por enquanto. Elas retornam uma `UnsupportedOperationException` com uma mensagem explicativa.

## Validação de Opções

Todas as operações avançadas incluem validação específica:

- **PDF_CROP**: Valida que os parâmetros x, y, width, height sejam números válidos
- **PDF_REORDER**: Valida que page_order seja fornecido
- **PDF_RESIZE**: Valida que page_size seja um dos valores suportados (A4, A3, A5, LETTER, LEGAL)
- **COMPRESS**: Aceita qualquer qualidade (implementação básica)

## Esquemas de Opções

Cada operação tem seu esquema de opções documentado e pode ser consultado via:

```bash
curl -X GET "http://localhost:8080/api/v1/operations" \
  -H "X-API-Key: your-api-key"
```

## Limitações Atuais

1. **PDF_TO_IMAGES**: Requer PDFRenderer ou biblioteca similar
2. **IMAGES_TO_PDF**: Requer processamento de imagens
3. **COMPRESS**: Implementação básica, não comprime imagens ou otimiza fontes
4. **PDF_CROP**: Usa importPage + setCropBox (pode não funcionar perfeitamente em todos os casos)

## Próximos Passos

Para uma implementação mais robusta, considere:

1. Adicionar Apache PDFBox Tools para PDF_TO_IMAGES
2. Implementar compressão avançada com otimização de imagens
3. Melhorar o crop para funcionar com conteúdo complexo
4. Adicionar mais formatos de página para resize