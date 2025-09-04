# Exemplos de Uso da API - Operações Avançadas

Este arquivo contém exemplos práticos de uso das operações avançadas de PDF usando curl.

## Autenticação

Todos os exemplos assumem que você possui uma API key válida. Substitua `YOUR_API_KEY` pela sua chave real.

## PDF_EDIT - Edição de Texto

### Adicionar Texto

```bash
# Adicionar texto "CONFIDENCIAL" na posição (100, 200) da página 1
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_EDIT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "edited_document.pdf",
    "edit_type": "add_text",
    "text": "CONFIDENCIAL",
    "x": 100,
    "y": 200,
    "page": 1,
    "font_size": 14,
    "color": "#FF0000"
  }'
```

### Substituir Texto

```bash
# Substituir "Texto Antigo" por "Texto Novo"
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_EDIT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "replaced_document.pdf",
    "edit_type": "replace_text",
    "text": "Texto Antigo",
    "new_text": "Texto Novo",
    "font_size": 12,
    "color": "#000000"
  }'
```

### Remover Texto

```bash
# Remover texto específico do documento
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_EDIT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "cleaned_document.pdf",
    "edit_type": "remove_text",
    "text": "Texto a ser removido"
  }'
```

## PDF_PROTECT - Proteção com Senha

### Proteção Básica

```bash
# Proteger PDF com senha do usuário
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_PROTECT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "protected_document.pdf",
    "user_password": "user123",
    "owner_password": "owner456"
  }'
```

### Proteção com Permissões Específicas

```bash
# Proteger PDF com controle de permissões
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_PROTECT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "restricted_document.pdf",
    "user_password": "user123",
    "owner_password": "owner456",
    "allow_printing": true,
    "allow_copying": false,
    "allow_modification": false,
    "allow_annotation": true
  }'
```

### Proteção Somente Leitura

```bash
# Criar PDF somente leitura (sem senha do usuário)
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_PROTECT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "readonly_document.pdf",
    "owner_password": "owner456",
    "allow_printing": true,
    "allow_copying": true,
    "allow_modification": false,
    "allow_annotation": false
  }'
```

## PDF_UNLOCK - Remoção de Proteção

### Desbloquear PDF Protegido

```bash
# Remover proteção de PDF com senha
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_UNLOCK" \
  -F "files=@protected.pdf" \
  -F 'options={
    "output_filename": "unlocked_document.pdf",
    "password": "user123"
  }'
```

### Desbloquear com Senha do Proprietário

```bash
# Usar senha do proprietário para desbloquear
curl -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_UNLOCK" \
  -F "files=@protected.pdf" \
  -F 'options={
    "output_filename": "fully_unlocked_document.pdf",
    "password": "owner456"
  }'
```

## Verificar Status do Job

```bash
# Verificar o status de um job (substitua JOB_ID pelo ID retornado)
curl -X GET "http://localhost:8080/api/v1/jobs/JOB_ID" \
  -H "X-API-Key: YOUR_API_KEY"
```

## Download do Resultado

```bash
# Fazer download do arquivo processado
curl -X GET "http://localhost:8080/api/v1/jobs/JOB_ID/download" \
  -H "X-API-Key: YOUR_API_KEY" \
  -o resultado.pdf
```

## Fluxo Completo de Exemplo

```bash
#!/bin/bash

# 1. Criar job de edição
JOB_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/v1/jobs" \
  -H "Content-Type: multipart/form-data" \
  -H "X-API-Key: YOUR_API_KEY" \
  -F "operation=PDF_EDIT" \
  -F "files=@input.pdf" \
  -F 'options={
    "output_filename": "edited.pdf",
    "edit_type": "add_text",
    "text": "PROCESSADO",
    "x": 50,
    "y": 50,
    "font_size": 16,
    "color": "#0000FF"
  }')

# 2. Extrair job ID
JOB_ID=$(echo $JOB_RESPONSE | jq -r '.jobId')
echo "Job criado: $JOB_ID"

# 3. Aguardar conclusão
while true; do
  STATUS=$(curl -s -X GET "http://localhost:8080/api/v1/jobs/$JOB_ID" \
    -H "X-API-Key: YOUR_API_KEY" | jq -r '.status')
  
  echo "Status: $STATUS"
  
  if [ "$STATUS" = "COMPLETED" ]; then
    break
  elif [ "$STATUS" = "FAILED" ]; then
    echo "Job falhou!"
    exit 1
  fi
  
  sleep 2
done

# 4. Download do resultado
curl -X GET "http://localhost:8080/api/v1/jobs/$JOB_ID/download" \
  -H "X-API-Key: YOUR_API_KEY" \
  -o edited_result.pdf

echo "Arquivo processado salvo como: edited_result.pdf"
```

## Notas Importantes

- Substitua `YOUR_API_KEY` pela sua chave de API real
- Os arquivos de entrada devem existir no caminho especificado
- Verifique sempre o status do job antes de tentar fazer o download
- Para operações de edição de texto, as coordenadas (x, y) são em pontos PDF (72 pontos = 1 polegada)
- As cores devem ser especificadas em formato hexadecimal (#RRGGBB)
- Senhas devem ser strings válidas e não podem estar vazias