# PDF Processing API - Exemplos e CLI de Desenvolvimento

Este diretório contém exemplos práticos e ferramentas CLI para facilitar o desenvolvimento e teste da API de processamento de PDF.

## 📁 Arquivos Disponíveis

### 🔧 Scripts CLI

- **`curl-examples.sh`** - Script com exemplos completos de uso da API via curl
- **`pdf-cli.sh`** - CLI interativo para desenvolvimento e teste

### 📖 Documentação

- **`README.md`** - Este arquivo com instruções de uso

## 🚀 Como Usar

### Pré-requisitos

1. **API rodando**: Certifique-se de que a API está executando:
   ```bash
   cd /home/otavio/API-JAVA
   mvn spring-boot:run -pl app/api
   ```

2. **Dependências**: Os scripts requerem:
   - `curl` - Para requisições HTTP
   - `jq` - Para formatação JSON (opcional, mas recomendado)
   - `bash` - Shell para execução

### 📋 Script de Exemplos (curl-examples.sh)

Script não-interativo que demonstra todas as operações da API:

```bash
# Executar todos os exemplos
./examples/curl-examples.sh

# Ver apenas a ajuda
./examples/curl-examples.sh help
```

**Funcionalidades:**
- ✅ Verificação automática do status da API
- 📋 Lista todas as operações suportadas
- 🔍 Mostra esquemas de opções para operações
- 📝 Exemplos completos de todas as operações
- 🎨 Output colorido para melhor visualização

### 🖥️ CLI Interativo (pdf-cli.sh)

CLI completo para desenvolvimento com modo interativo:

```bash
# Modo interativo
./examples/pdf-cli.sh

# Comandos diretos
./examples/pdf-cli.sh status
./examples/pdf-cli.sh operations
./examples/pdf-cli.sh help
```

## 📚 Comandos do CLI Interativo

### 🔍 Informações e Status

| Comando | Descrição |
|---------|----------|
| `help`, `h` | Mostrar ajuda completa |
| `status`, `s` | Verificar se a API está rodando |
| `operations`, `ops` | Listar operações suportadas |
| `schema <operation>` | Mostrar esquema de opções |
| `examples` | Mostrar exemplos de uso |

### 📁 Gerenciamento de Arquivos

| Comando | Descrição |
|---------|----------|
| `upload <file>` | Fazer upload de arquivo |
| `download <file-id>` | Baixar arquivo por ID |

### ⚙️ Gerenciamento de Jobs

| Comando | Descrição |
|---------|----------|
| `job <operation>` | Criar job interativo |
| `jobs` | Listar todos os jobs |
| `job-status <job-id>` | Verificar status do job |

### 🚀 Operações Rápidas

| Comando | Descrição |
|---------|----------|
| `quick-merge` | Merge rápido de 2 PDFs |
| `quick-split` | Split rápido de PDF |
| `quick-watermark` | Watermark rápido |
| `quick-crop` | Crop rápido |

### 🛠️ Configuração e Limpeza

| Comando | Descrição |
|---------|----------|
| `config` | Configurar URL base da API |
| `clean` | Limpar arquivos temporários |
| `exit`, `quit`, `q` | Sair do CLI |

## 🎯 Exemplos de Uso Prático

### 1. Workflow Básico - Merge de PDFs

```bash
# Iniciar CLI
./examples/pdf-cli.sh

# No CLI:
pdf-cli> upload documento1.pdf
pdf-cli> upload documento2.pdf
pdf-cli> quick-merge
pdf-cli> job-status <job-id>
pdf-cli> download <result-file-id>
```

### 2. Operação Avançada - Crop de PDF

```bash
# No CLI:
pdf-cli> job PDF_CROP
# Digite as opções JSON:
{
  "inputFile": "documento.pdf",
  "x": 50,
  "y": 50,
  "width": 400,
  "height": 600,
  "pages": "1-5",
  "outputFileName": "cropped.pdf"
}
```

### 3. Verificação de Esquemas

```bash
# Ver esquema de uma operação
pdf-cli> schema WATERMARK
pdf-cli> schema PDF_REORDER
pdf-cli> schema COMPRESS
```

## 🔧 Configuração Avançada

### Arquivo de Configuração

O CLI salva configurações em `~/.pdf-cli-config`:

```bash
API_BASE="http://localhost:8080/api/v1"
LAST_JOB_ID="job-123"
LAST_FILE_ID="file-456"
```

### Personalização da URL Base

```bash
# No CLI:
pdf-cli> config
# Digite nova URL: http://production-api:8080/api/v1
```

### Arquivos Temporários

Arquivos baixados são salvos em `/tmp/pdf-cli/`:

```bash
# Limpar arquivos temporários
pdf-cli> clean
```

## 📋 Operações Suportadas

### 🔄 Operações Básicas
- **MERGE** - Combinar múltiplos PDFs
- **SPLIT** - Dividir PDF em páginas/intervalos
- **ROTATE** - Rotacionar páginas
- **WATERMARK** - Adicionar marca d'água
- **ENCRYPT** - Criptografar PDF
- **DECRYPT** - Descriptografar PDF
- **EXTRACT_TEXT** - Extrair texto
- **EXTRACT_METADATA** - Extrair metadados

### ⚡ Operações Avançadas (ETAPA 12)
- **PDF_CROP** - Cortar área específica
- **PDF_REORDER** - Reordenar páginas
- **PDF_RESIZE** - Redimensionar páginas
- **COMPRESS** - Compressão de PDF

## 🐛 Troubleshooting

### API não está rodando
```bash
# Verificar se a API está rodando
curl http://localhost:8080/api/v1/health

# Se não estiver, iniciar:
cd /home/otavio/API-JAVA
mvn spring-boot:run -pl app/api
```

### Erro de permissão nos scripts
```bash
# Tornar executáveis
chmod +x examples/*.sh
```

### jq não instalado
```bash
# Ubuntu/Debian
sudo apt-get install jq

# CentOS/RHEL
sudo yum install jq
```

### Arquivos temporários ocupando espaço
```bash
# Limpar manualmente
rm -rf /tmp/pdf-cli/*

# Ou usar o comando do CLI
pdf-cli> clean
```

## 🎨 Recursos do CLI

### 🌈 Output Colorido
- 🔵 Azul: Informações e títulos
- 🟢 Verde: Sucesso e confirmações
- 🟡 Amarelo: Avisos e prompts
- 🔴 Vermelho: Erros
- 🟣 Roxo: Títulos principais
- 🔷 Ciano: Dados e exemplos

### 💾 Persistência de Estado
- Último Job ID usado
- Último File ID usado
- URL base configurada
- Configurações salvas automaticamente

### 🔄 Funcionalidades Inteligentes
- Auto-sugestão de IDs anteriores
- Exemplos automáticos para operações
- Verificação automática de status
- Download automático de resultados

## 📞 Suporte

Para problemas ou dúvidas:

1. **Verificar logs da API**: Console onde a API está rodando
2. **Testar conectividade**: `curl http://localhost:8080/api/v1/health`
3. **Verificar arquivos**: Certificar que os PDFs existem e são válidos
4. **Consultar esquemas**: Usar `schema <operation>` para ver opções válidas

## 🚀 Próximos Passos

Após dominar estes exemplos, você pode:

1. **Integrar com sua aplicação**: Usar os exemplos curl como base
2. **Automatizar workflows**: Criar scripts personalizados
3. **Monitorar performance**: Usar os comandos de status
4. **Explorar operações avançadas**: Testar PDF_CROP, PDF_REORDER, etc.

---

**Desenvolvido para facilitar o desenvolvimento e teste da API de Processamento de PDF** 🎯