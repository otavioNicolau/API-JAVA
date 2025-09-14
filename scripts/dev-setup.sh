#!/bin/bash

# Script de configuração do ambiente de desenvolvimento
# Instala dependências e configura o ambiente

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

echo "🛠️ Configurando ambiente de desenvolvimento..."

# Detectar sistema operacional
OS="$(uname -s)"
case "${OS}" in
    Linux*)     MACHINE=Linux;;
    Darwin*)    MACHINE=Mac;;
    *)          MACHINE="UNKNOWN:${OS}"
esac

log_info "Sistema detectado: $MACHINE"

# Verificar Java
log_info "Verificando Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log_success "Java encontrado: $JAVA_VERSION"
    
    # Verificar se é Java 21+
    MAJOR_VERSION=$(echo $JAVA_VERSION | cut -d'.' -f1)
    if [ "$MAJOR_VERSION" -ge 21 ]; then
        log_success "Java 21+ detectado"
    else
        log_warning "Java 21 é recomendado. Versão atual: $JAVA_VERSION"
    fi
else
    log_error "Java não encontrado!"
    echo "Instale Java 21:"
    case $MACHINE in
        Linux)
            echo "  sudo apt update && sudo apt install openjdk-21-jdk"
            ;;
        Mac)
            echo "  brew install openjdk@21"
            ;;
    esac
    exit 1
fi

# Verificar Maven
log_info "Verificando Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    log_success "Maven encontrado: $MVN_VERSION"
else
    log_error "Maven não encontrado!"
    echo "Instale Maven:"
    case $MACHINE in
        Linux)
            echo "  sudo apt update && sudo apt install maven"
            ;;
        Mac)
            echo "  brew install maven"
            ;;
    esac
    exit 1
fi

# Verificar Redis
log_info "Verificando Redis..."
if command -v redis-server &> /dev/null; then
    REDIS_VERSION=$(redis-server --version | cut -d'=' -f2 | cut -d' ' -f1)
    log_success "Redis encontrado: $REDIS_VERSION"
else
    log_warning "Redis não encontrado!"
    echo "Para instalar Redis:"
    case $MACHINE in
        Linux)
            echo "  sudo apt update && sudo apt install redis-server"
            ;;
        Mac)
            echo "  brew install redis"
            ;;
    esac
fi

# Verificar Tesseract (opcional)
log_info "Verificando Tesseract OCR..."
if command -v tesseract &> /dev/null; then
    TESSERACT_VERSION=$(tesseract --version | head -n 1 | cut -d' ' -f2)
    log_success "Tesseract encontrado: $TESSERACT_VERSION"
else
    log_warning "Tesseract não encontrado (opcional para OCR)"
    echo "Para instalar Tesseract:"
    case $MACHINE in
        Linux)
            echo "  sudo apt install tesseract-ocr tesseract-ocr-por tesseract-ocr-eng"
            ;;
        Mac)
            echo "  brew install tesseract tesseract-lang"
            ;;
    esac
fi

# Criar estrutura de diretórios
log_info "Criando estrutura de diretórios..."
mkdir -p storage/{jobs,results,temp}
mkdir -p logs
log_success "Diretórios criados"

# Tornar scripts executáveis
log_info "Configurando permissões dos scripts..."
chmod +x scripts/*.sh
log_success "Permissões configuradas"

# Configurar Git hooks (se for repositório Git)
if [ -d ".git" ]; then
    log_info "Configurando Git hooks..."
    
    # Pre-commit hook para formatação
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "Executando formatação antes do commit..."
./scripts/format.sh
git add -A
EOF
    chmod +x .git/hooks/pre-commit
    log_success "Git hooks configurados"
fi

# Criar arquivo de configuração local
log_info "Criando configuração local..."
cat > .env.local << 'EOF'
# Configurações locais do PDF Processor API
API_KEY=dev123
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
REDIS_HOST=localhost
REDIS_PORT=6379
WORKER_THREADS=2

# Configurações de desenvolvimento
DEBUG=true
LOG_LEVEL=DEBUG
EOF
log_success "Arquivo .env.local criado"

echo ""
log_success "🎉 Ambiente de desenvolvimento configurado!"
echo ""
echo "Próximos passos:"
echo "  1. Executar build: ./scripts/build.sh"
echo "  2. Iniciar Redis: ./scripts/start-redis.sh"
echo "  3. Executar testes: ./scripts/test.sh"
echo "  4. Iniciar API: ./scripts/run-api.sh"
echo "  5. Iniciar Worker: ./scripts/run-worker.sh"
echo ""
echo "Documentação: http://localhost:8080/swagger-ui"
