# OBJETIVO
Quero que você crie uma **API REST de processamento assíncrono de PDFs** em **Java** com **Clean Architecture**, **sem Docker**. 
Você VAI TRABALHAR EM 15 ETAPAS CURTAS. Em cada etapa:
1) Especifique claramente o que vai entregar,
2) gere o menor código possível,
3) rode testes (explique como rodar),
4) valide resultado com exemplos (curl),
5) só avance quando a etapa estiver verde.
Seja **chato** com qualidade: rode testes a cada mudança, adicione checagens, linters e explique como verificar. Não pule passos.

# RESTRIÇÕES IMPORTANTES
- Linguagem: **Java 21** (pode aceitar 17 se necessário, mas padronize 21).
- Build: **Maven** (multi-módulo).
- Framework web: **Spring Boot**.
- Fila/Assíncrono: **Redis (local, sem Docker)** usando **Redis Streams** OU listas + bloqueio (escolha e justifique).
- PDF: **Apache PDFBox** (operações: merge, split, rotate, watermark, encrypt/decrypt, metadata, extract text). 
- PDF → imagens: **PDFBox PDFRenderer** + **TwelveMonkeys ImageIO**.
- Imagens → PDF: PDFBox (montagem de páginas com imagens).
- OCR: **Tess4J** (Tesseract instalado localmente).
- PDF/A & validação: **PDFBox Preflight** (converter "best effort"); validação com **veraPDF** opcional via CLI (somente se der para chamar localmente, sem Docker).
- Documentação: **OpenAPI 3** com **springdoc-openapi** (Swagger UI).
- Monitoramento: **Spring Boot Actuator** + **Micrometer**.
- Qualidade: **JUnit 5**, **Mockito**, **MockMvc**, **SpotBugs**, **Checkstyle/Google style**, **Spotless**.
- **NÃO USAR DOCKER** (nem Docker Compose, nem Testcontainers).
- Armazenamento: **filesystem local** (./storage).
- Segurança: Header **X-API-Key** simples.

# ARQUITETURA (padrão de pastas)
- root
  - pom.xml (pai)
  - app/
    - api/            (Spring Boot REST, controllers, DTOs, segurança, OpenAPI)
    - worker/         (Spring Boot CLI/worker que consome a fila Redis)
    - application/    (casos de uso, DTOs)
    - domain/         (entidades, portas)
    - infrastructure/ (PDF services, Redis, storage)
  - openapi/          (openapi.yaml)
  - scripts/          (shell/ps1 para desenvolvimento local)
  - tests/            (recursos compartilhados de teste, se precisar)

# ENDPOINTS (contrato estável)
- POST /v1/jobs         -> cria job (multipart/form-data com "operation", "files"/"urls", "options" json)
- GET  /v1/jobs/{id}    -> status/progresso
- GET  /v1/jobs/{id}/download -> baixa resultado
- (opcional) GET /v1/jobs/{id}/events -> SSE com progresso
- GET  /actuator/health -> health
- GET  /swagger-ui      -> UI

# OPERAÇÕES BÁSICAS (primeiro conjunto)
merge, split, compress (básico, reamostrar imagens quando possível), pdf_to_images, images_to_pdf, rotate, watermark (texto/imagem simples), encrypt, decrypt, extract_metadata, extract_text.

# OPERAÇÕES AVANÇADAS (depois)
pdf_crop, pdf_reorder, pdf_resize, pdf_compare (visual/textual simples), pdf_create (texto/HTML/imagens), pdf_edit (anotações simples), pdf_protect/pdf_unlock, pdf_optimize, pdf_validate, pdf_repair, pdf_extract_resources, pdf_remove_resources, pdf_to_pdfa, pdf_ocr, pdf_to_audio (TTS após extrair texto), pdf_from_epub, pdf_from_djvu (opcional, só se viável sem Docker).

# ETAPAS (faça EXATAMENTE na ordem, sem pular)

ETAPA 01 — Bootstrap do projeto (Maven multi-módulo)
- Entregue: pom.xml pai e módulos (api, worker, application, domain, infrastructure).
- Inclua dependências mínimas: Spring Boot Web/Validation, springdoc-openapi, Actuator, Spring Data Redis (Lettuce), PDFBox, Tess4J (placeholder), TwelveMonkeys ImageIO, JUnit/Mockito/MockMvc, Spotless/Checkstyle/SpotBugs.
- Crie um README curto com pré-requisitos (JDK, Redis local, Tesseract opcional).
- Testes: compile, rode `mvn -q -DskipTests=false test`.
- Sem código de negócios ainda.

ETAPA 02 — Clean Architecture & contratos
- Defina interfaces no **domain** (Job, JobStatus, JobOperation enum, portas de fila e storage).
- No **application**, defina casos de uso: CreateJobUseCase, GetJobStatusUseCase, DownloadResultUseCase.
- No **infrastructure**, esboce adaptadores RedisQueue e LocalStorage (sem implementação completa).
- Testes: unitários dos DTOs/enums (simples), verifique naming e empacotamento.

ETAPA 03 — API mínima + OpenAPI
- No **api**, crie controlador `JobController` com:
  - POST /v1/jobs (recebe multipart com operation/files/options, apenas valida e cria job stub chamando use case),
  - GET /v1/jobs/{id},
  - GET /v1/jobs/{id}/download (ainda retorna 404 se não pronto).
- Configure **springdoc-openapi** e mostre `/swagger-ui`.
- Segurança: filtro simples que exige `X-API-Key` (chave em env `API_KEY`).
- Testes: MockMvc para 201 (criação) e 400 (validação).

ETAPA 04 — Fila Redis (local, sem Docker)
- Implemente **RedisQueue** (Streams ou listas + BRPOP). Documente a escolha.
- Publique mensagens de job (id, operation, file refs, options).
- Crie comandos para subir Redis local (Windows/Linux) e script `scripts/check_redis.sh` para testar conexão.
- Testes: integração leve usando **embedded-redis** (se estável no seu ambiente) OU stub com contrato.

ETAPA 05 — Storage local
- Implemente **LocalStorageService**: salvar uploads, gerar caminho determinístico por jobId, armazenar artefatos (`result.pdf`, `page-1.png`, etc.).
- Valide limites de tamanho e extensões, sanitize nomes.
- Testes: unidade do storage (criar, ler, remover), cobrindo erros.

ETAPA 06 — Worker
- **worker/**: app Spring Boot CLI que:
  - Consome a fila,
  - Atualiza status/progresso em Redis,
  - Chama um **PdfService** (na infrastructure) por operação.
- Implemente loop robusto (retry com backoff, DLQ simples).
- Testes: unidade do orquestrador do worker (com fila stub).

ETAPA 07 — PDFService (básico) com PDFBox
- Entregue implementações para: **merge, split, rotate, extract_text, extract_metadata**.
- **compress (básico)**: reamostrar imagens quando viável; documente limitações do PDFBox.
- **pdf_to_images**: PDFRenderer + ImageIO; **images_to_pdf**: montar páginas a partir de imagens.
- Testes: unidade por operação (entrada mínima/saída esperada). Forneça PDFs de exemplo em `tests/resources/`.

ETAPA 08 — Watermark & Encrypt/Decrypt
- Watermark texto/imagem simples (desenho por PDPageContentStream).
- Encrypt/Decrypt com senha e permissões.
- Testes: validar que o arquivo de saída abre e que permissões/visuais fazem efeito.

ETAPA 09 — API: fluxo completo de job
- POST cria job, salva arquivos, publica na fila, retorna jobId.
- GET status lê do Redis.
- GET download retorna artefato principal (ou ZIP quando operação gera múltiplos).
- Adicione paginação/listagem básica de jobs (opcional).
- Testes: MockMvc ponta-a-ponta com stubs (sem Docker).

ETAPA 10 — Monitoramento & qualidade
- Actuator `/actuator/health`, `/actuator/metrics`.
- Linters: Spotless + Checkstyle + SpotBugs configurados no build (falhem em violações).
- Scripts `scripts/` para: `format`, `lint`, `test`, `run-api`, `run-worker`.
- Testes: garanta que o pipeline `mvn verify` roda limpo.

ETAPA 11 — Documentação OpenAPI
- Mantenha contrato: 
  - POST /v1/jobs (multipart: `operation`, `files`/`urls`, `options` json),
  - GET /v1/jobs/{id},
  - GET /v1/jobs/{id}/download.
- Gere exemplos por operação básica.
- Valide no `/swagger-ui`.

ETAPA 12 — Operações AVANÇADAS (contratos de options)
Implemente progressivamente (uma por vez, com testes e exemplos):
- `pdf_crop`
  {
    "mode":"margins|box|bbox",
    "margins_mm":{"top":5,"right":5,"bottom":5,"left":5},
    "box":{"x":0,"y":0,"width":500,"height":700,"unit":"pt|mm"},
    "pages":"all|1,3,7-10"
  }
- `pdf_reorder` { "order":[3,1,2,5,4], "fill_missing":false }
- `pdf_resize`
  {
    "page_size":"A4|Letter|Legal|Custom",
    "custom_size_mm":{"width":210,"height":297},
    "scale":1.0,
    "margin_mm":0,
    "fit":"contain|cover|stretch",
    "pages":"all|1-5,7"
  }
- `pdf_compare` (visual/texto simples, “best effort” com PDFBox)
  {
    "mode":"visual|text","tolerance":0.1,
    "highlight":"diff|side-by-side","output":"pdf|zip|json","pages":"all"
  }
- `pdf_create` (texto/HTML/imagens; para HTML pode renderizar simplificado)
  {
    "source":"text|html|images",
    "text":"...","html":"<html>...</html>","images":["a.jpg","b.png"],
    "page_size":"A4","margin_mm":10,
    "header_footer":{"header_html":"...","footer_html":"pág. {{page}} de {{pages}}"}
  }
- `pdf_edit` (anotações simples)
  {
    "actions":[
      {"type":"text","page":1,"x":50,"y":700,"text":"Rascunho","size":18,"opacity":0.6},
      {"type":"rect","page":2,"x":40,"y":500,"w":200,"h":80,"stroke":true,"fill":false},
      {"type":"pagenumber","range":"all","pos":"bottom-center","format":"Pág. {{n}}"}
    ]
  }
- `pdf_protect` / `pdf_unlock`
  {"user_password":"1234","owner_password":"admin","permissions":["print","copy","annotate"]}
  {"password":"1234","attempt_repair":true}
- `pdf_optimize`
  {"image":{"downscale_dpi":150,"recompress":true,"format":"jpeg","quality":0.7},"remove_metadata":true,"linearize":true,"subset_fonts":true}
- `pdf_validate` {"profile":"pdf|pdfa","report":"json|pdf|txt"}
- `pdf_repair` {"aggressiveness":"low|medium|high","try_recover_xref":true}
- `pdf_extract_resources`
  {"types":["images","fonts","attachments","metadata"],"images":{"export_format":"png|jpeg","dpi":300},"fonts":{"include_subsetting":true},"attachments":true,"output":"zip|json"}
- `pdf_remove_resources` {"remove":["attachments","javascript","thumbnails","metadata"],"safe_mode":true}
- `pdf_to_pdfa` {"flavor":"A-1b|A-2b|A-2u|A-3b","icc_profile":"sRGB.icc","fix_metadata":true}
- `pdf_ocr` {"lang":"por+eng","dpi":300,"deskew":true,"clean_background":true,"output":"pdf|pdfa"}
- `pdf_to_audio` {"voice":"pt-BR-Female|pt-BR-Male","speed":1.0,"format":"mp3|wav","pages":"all|1-5"}
- `pdf_from_epub` {"page_size":"A4","margin_mm":10,"chapter_toc":true}
- `pdf_from_djvu` {"dpi":300,"binarize":false}
Observação: implemente o que for **viável sem Docker**; documente limitações em cada operação.

ETAPA 13 — Exemplos & CLI de desenvolvimento
- Forneça **exemplos curl** prontos:
  - Criar job merge:
    curl -X POST http://localhost:8080/v1/jobs \
      -H "X-API-Key: dev123" \
      -F "operation=merge" \
      -F "files=@a.pdf" -F "files=@b.pdf" \
      -F 'options={"output_filename":"merged.pdf"}'
  - Status:
    curl http://localhost:8080/v1/jobs/{id} -H "X-API-Key: dev123"
  - Download:
    curl -O -J http://localhost:8080/v1/jobs/{id}/download -H "X-API-Key: dev123"
- Crie um pequeno **CLI Java** (opcional) para disparar jobs localmente.

ETAPA 14 — Segurança, limites e hardening
- Validação rigorosa de entrada; limites de tamanho/total de páginas.
- Sanitização de nomes; timeouts por operação; quotas simples por IP/chave.
- Logs de auditoria e métricas por operação (Micrometer).
- Testes de segurança (negativos): uploads inválidos, opções incompatíveis, etc.

ETAPA 15 — Documentação final e guia de uso
- README completo (instalação do Redis local, Tesseract, como rodar API e Worker em terminais separados).
- `openapi/openapi.yaml` atualizado.
- Secção “Erros comuns” (400, 401, 409, 413, 422, 500) e como diagnosticar.
- Check-list de qualidade: `mvn spotless:apply verify` deve passar limpo.

# NOTAS DE PERFORMANCE (sem Docker)
- Use pool de threads configurável (Spring TaskExecutor).
- Para jobs pesados (OCR, optimize, pdf_to_pdfa), execute no Worker e produza ZIP quando multi-artefatos.
- Considere backpressure na fila Redis (Streams com consumer groups).

# O QUE TESTAR SEMPRE (seja CHATO)
- Cada operação: um teste mínimo que prova a saída (tamanho > 0, páginas esperadas, metadados, permissões).
- Erros previsíveis: senha errada, PDF quebrado, options inválidas.
- Performance básica: medir tempo de merge/split em pequenos PDFs.
- Segurança do header X-API-Key (403 quando ausente).

# LEMBRETES
- Nada de Docker/Testcontainers. Tudo **local**.
- Se algo não for possível 100% com PDFBox/Tess4J, entregue **“best effort”** e documente a limitação.
- Sempre mostre como rodar: `mvn clean verify`, `java -jar app/api/target/api.jar`, `java -jar app/worker/target/worker.jar`.
