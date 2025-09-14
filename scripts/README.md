# Scripts de Desenvolvimento - PDF Processor API

Esta pasta contém scripts utilitários para facilitar o desenvolvimento, build e execução do projeto PDF Processor API.

## 📋 Scripts Disponíveis

### 🔧 Configuração e Build

- **`dev-setup.sh`** - Configuração inicial do ambiente de desenvolvimento
  - Verifica dependências (Java 21, Maven, Redis, Tesseract)
  - Cria estrutura de diretórios
  - Configura permissões e Git hooks
  - Cria arquivo de configuração local

- **`build.sh`** - Build completo do projeto
  - Limpa builds anteriores
  - Compila todos os módulos
  - Aplica formatação de código
  - Executa verificações de qualidade
  - Gera JARs executáveis

### 🚀 Execução de Serviços

- **`start-redis.sh`** - Inicia Redis local
  - Detecta sistema operacional
  - Inicia Redis via systemctl/brew/daemon
  - Verifica conectividade

- **`run-api.sh`** - Executa a API REST
  - Verifica dependências (Redis)
  - Inicia servidor Spring Boot na porta 8080
  - Configura variáveis de ambiente
  - Monitora status de saúde

- **`run-worker.sh`** - Executa o Worker assíncrono
  - Verifica dependências (Redis)
  - Inicia processador de jobs
  - Configura pool de threads

- **`stop-services.sh`** - Para todos os serviços
  - Para API, Worker e opcionalmente Redis
  - Força parada se necessário
  - Verifica processos restantes

### 🧪 Qualidade e Testes

- **`test.sh`** - Executa todos os testes
  - Testes unitários
  - Testes de integração
  - Relatórios de cobertura
  - Análise estática (SpotBugs)

- **`format.sh`** - Formata código automaticamente
  - Aplica Google Java Style via Spotless
  - Remove imports não utilizados
  - Formata anotações

- **`lint.sh`** - Verificações de qualidade
  - Checkstyle
  - SpotBugs
  - Verificação de formatação
  - Compilação

### 📚 Exemplos e Documentação

- **`examples.sh`** - Exemplos interativos de uso da API
  - Demonstrações com curl
  - Operações básicas e avançadas
  - Monitoramento via SSE

## 🚀 Fluxo de Desenvolvimento Recomendado

### Primeira Configuração
```bash
# 1. Configurar ambiente
./scripts/dev-setup.sh

# 2. Build inicial
./scripts/build.sh

# 3. Executar testes
./scripts/test.sh
```

### Desenvolvimento Diário
```bash
# 1. Iniciar Redis
./scripts/start-redis.sh

# 2. Executar API (terminal 1)
./scripts/run-api.sh

# 3. Executar Worker (terminal 2)
./scripts/run-worker.sh

# 4. Testar API
./scripts/examples.sh
```

### Antes de Commit
```bash
# 1. Formatar código
./scripts/format.sh

# 2. Verificar qualidade
./scripts/lint.sh

# 3. Executar testes
./scripts/test.sh

# 4. Build final
./scripts/build.sh
```

## 🔧 Variáveis de Ambiente

Os scripts suportam as seguintes variáveis de ambiente:

```bash
# API Configuration
API_KEY=dev123                    # Chave de API para desenvolvimento
SPRING_PROFILES_ACTIVE=dev        # Profile do Spring Boot
SERVER_PORT=8080                  # Porta da API

# Redis Configuration
REDIS_HOST=localhost              # Host do Redis
REDIS_PORT=6379                   # Porta do Redis

# Worker Configuration
WORKER_THREADS=2                  # Número de threads do worker

# Development
DEBUG=true                        # Habilitar debug
LOG_LEVEL=DEBUG                   # Nível de log
```

## 📁 Estrutura de Diretórios Criada

```
storage/
├── jobs/          # Arquivos de entrada dos jobs
├── results/       # Resultados processados
└── temp/          # Arquivos temporários

logs/              # Logs da aplicação
```

## 🛠️ Dependências Necessárias

### Obrigatórias
- **Java 21+** (ou Java 17 como alternativa)
- **Maven 3.8+**
- **Redis** (instalação local)

### Opcionais
- **Tesseract OCR** (para funcionalidades de OCR)
- **Git** (para hooks de desenvolvimento)

## 📖 Documentação da API

Após iniciar a API, acesse:
- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## 🐛 Troubleshooting

### Redis não inicia
```bash
# Verificar se Redis está instalado
redis-server --version

# Verificar se porta está livre
lsof -i :6379

# Logs do Redis (Linux)
sudo journalctl -u redis-server
```

### API não responde
```bash
# Verificar se porta está livre
lsof -i :8080

# Verificar logs
tail -f logs/application.log

# Verificar health check
curl http://localhost:8080/actuator/health
```

### Build falha
```bash
# Limpar cache do Maven
mvn clean

# Verificar versão do Java
java -version

# Executar com debug
mvn compile -X
```

## 📞 Suporte

Para problemas ou sugestões:
1. Verificar logs em `logs/`
2. Consultar documentação OpenAPI
3. Executar `./scripts/test.sh` para diagnósticos
4. Verificar issues conhecidas no README.md principal
