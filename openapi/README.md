# PDF Processor API - Documentação OpenAPI

Este diretório contém a documentação completa da API PDF Processor em formato OpenAPI 3.0.

## Arquivos

- `openapi.yaml` - Especificação completa da API em formato YAML

## Como usar a documentação

### 1. Swagger UI (Recomendado)

A aplicação já inclui o Swagger UI integrado. Quando a API estiver rodando, acesse:

```
http://localhost:8080/swagger-ui/index.html
```

### 2. Visualizar o arquivo YAML

Você pode visualizar e editar o arquivo `openapi.yaml` em qualquer editor que suporte OpenAPI, como:

- [Swagger Editor](https://editor.swagger.io/)
- [Insomnia](https://insomnia.rest/)
- [Postman](https://www.postman.com/)
- VS Code com extensão OpenAPI

### 3. Gerar clientes

Use o OpenAPI Generator para gerar clientes em diferentes linguagens:

```bash
# Instalar o OpenAPI Generator
npm install @openapitools/openapi-generator-cli -g

# Gerar cliente Java
openapi-generator-cli generate -i openapi.yaml -g java -o ./client-java

# Gerar cliente Python
openapi-generator-cli generate -i openapi.yaml -g python -o ./client-python

# Gerar cliente JavaScript
openapi-generator-cli generate -i openapi.yaml -g javascript -o ./client-js
```

## Principais endpoints

### Jobs
- `POST /api/v1/jobs` - Criar novo job de processamento
- `GET /api/v1/jobs` - Listar todos os jobs
- `GET /api/v1/jobs/{jobId}` - Obter status de um job
- `GET /api/v1/jobs/{jobId}/download` - Download do resultado
- `DELETE /api/v1/jobs/{jobId}` - Cancelar job

### Health Check
- `GET /api/v1/health` - Status básico da aplicação
- `GET /api/v1/health/detailed` - Status detalhado com componentes

## Operações de PDF suportadas

### Básicas
- `MERGE` - Combinar múltiplos PDFs
- `SPLIT` - Dividir PDF em páginas
- `ROTATE` - Rotacionar páginas
- `COMPRESS` - Comprimir PDF
- `WATERMARK` - Adicionar marca d'água
- `ENCRYPT` - Criptografar PDF
- `DECRYPT` - Descriptografar PDF

### Avançadas
- `PDF_CROP` - Recortar páginas
- `PDF_REORDER` - Reordenar páginas
- `PDF_COMPARE` - Comparar PDFs
- `PDF_EDIT` - Editar conteúdo
- `PDF_PROTECT` - Proteger com senha
- `PDF_OPTIMIZE` - Otimizar tamanho e compressão
- `PDF_VALIDATE` - Validar estrutura e conformidade
- `PDF_REPAIR` - Reparar PDF corrompido
- `PDF_OCR` - Reconhecimento de texto
- `PDF_EXTRACT_TEXT` - Extrair texto
- `PDF_EXTRACT_IMAGES` - Extrair imagens
- `PDF_CONVERT_TO_IMAGE` - Converter para imagem
- `PDF_CONVERT_FROM_IMAGE` - Converter de imagem
- `PDF_ADD_ANNOTATIONS` - Adicionar anotações
- `PDF_REMOVE_ANNOTATIONS` - Remover anotações
- `PDF_FLATTEN` - Achatar formulários

## Autenticação

A API usa autenticação via API Key no header:

```
X-API-Key: sua-api-key-aqui
```

## Exemplos de uso

Veja exemplos práticos de uso da API no arquivo de documentação principal do projeto.

## Formatos suportados

### Entrada
- PDF (.pdf)
- Imagens (.png, .jpg, .jpeg, .gif, .bmp, .tiff)
- Documentos (.docx, .txt)

### Saída
- PDF (.pdf)
- Imagens (.png, .jpg, .jpeg)
- ZIP (para múltiplos arquivos)
- JSON (para dados extraídos)
- Texto (.txt)

## Limites

- Tamanho máximo por arquivo: 50MB
- Máximo de arquivos por job: 10
- Timeout de processamento: 5 minutos
- Rate limit: 100 requisições por minuto por API key