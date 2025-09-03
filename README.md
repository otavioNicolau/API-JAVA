# PDF Processor API

API REST de processamento assíncrono de PDFs em Java com Clean Architecture.

## Pré-requisitos

### Obrigatórios
- **JDK 21** (ou JDK 17 como alternativa)
- **Maven 3.8+**
- **Redis** (instalação local, sem Docker)

### Opcionais
- **Tesseract OCR** (para funcionalidades de OCR)
- **veraPDF** (para validação PDF/A avançada)

## Instalação do Redis (Local)

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
sudo systemctl enable redis-server
```

### CentOS/RHEL/Fedora
```bash
sudo dnf install redis
sudo systemctl start redis
sudo systemctl enable redis
```

### Windows
1. Baixe o Redis para Windows do repositório oficial
2. Extraia e execute `redis-server.exe`
3. Ou use WSL2 com as instruções do Ubuntu

### Verificar instalação
```bash
redis-cli ping
# Deve retornar: PONG
```

## Instalação do Tesseract (Opcional)

### Ubuntu/Debian
```bash
sudo apt install tesseract-ocr tesseract-ocr-por tesseract-ocr-eng
```

### CentOS/RHEL/Fedora
```bash
sudo dnf install tesseract tesseract-langpack-por tesseract-langpack-eng
```

### Windows
1. Baixe o instalador do Tesseract para Windows
2. Instale e adicione ao PATH
3. Configure a variável de ambiente `TESSDATA_PREFIX`

## Build e Execução

### Compilar o projeto
```bash
mvn clean compile
```

### Executar testes
```bash
mvn clean test
```

### Executar verificações de qualidade
```bash
mvn clean verify
```

### Formatar código
```bash
mvn spotless:apply
```

## Estrutura do Projeto

```
pdf-processor/
├── pom.xml                 # POM pai
├── app/
│   ├── api/               # REST API (Spring Boot Web)
│   ├── worker/            # Worker assíncrono (Spring Boot CLI)
│   ├── application/       # Casos de uso
│   ├── domain/            # Entidades e portas
│   └── infrastructure/    # Adaptadores (Redis, Storage, PDF)
├── openapi/               # Especificação OpenAPI
├── scripts/               # Scripts de desenvolvimento
└── tests/                 # Recursos de teste compartilhados
```

## Arquitetura

O projeto segue os princípios da **Clean Architecture**:

- **Domain**: Entidades de negócio e interfaces (portas)
- **Application**: Casos de uso e lógica de aplicação
- **Infrastructure**: Implementações concretas (adaptadores)
- **API**: Controllers REST e configurações web
- **Worker**: Processador assíncrono de jobs

## Tecnologias Utilizadas

- **Java 21** / **Spring Boot 3.2**
- **Maven** (multi-módulo)
- **Redis** (filas e cache)
- **Apache PDFBox** (manipulação de PDF)
- **Tess4J** (OCR)
- **TwelveMonkeys ImageIO** (processamento de imagens)
- **OpenAPI 3** / **Swagger UI** (documentação)
- **Spring Boot Actuator** / **Micrometer** (monitoramento)
- **JUnit 5** / **Mockito** (testes)
- **Spotless** / **Checkstyle** / **SpotBugs** (qualidade de código)

## Próximos Passos

1. Implementar as interfaces do domain
2. Criar casos de uso na camada application
3. Desenvolver a API REST
4. Implementar o worker assíncrono
5. Adicionar operações de processamento de PDF

## Licença

Este projeto é licenciado sob a MIT License.