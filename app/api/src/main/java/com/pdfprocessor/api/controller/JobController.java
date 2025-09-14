package com.pdfprocessor.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.pdfprocessor.api.exception.SecurityValidationException;
import com.pdfprocessor.api.service.InputValidationService;
import com.pdfprocessor.api.service.RateLimitService;
import com.pdfprocessor.application.dto.CreateJobRequest;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.CancelJobUseCase;
import com.pdfprocessor.application.usecase.CreateJobUseCase;
import com.pdfprocessor.application.usecase.DownloadResultUseCase;
import com.pdfprocessor.application.usecase.GetJobStatusUseCase;
import com.pdfprocessor.application.usecase.ListAllJobsUseCase;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Controller REST para operações de jobs de processamento de PDF. */
@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs", description = "Operações de jobs de processamento de PDF")
public class JobController {

  // Limites de segurança
  private static final long MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
  private static final int MAX_FILES_PER_JOB = 10;

  private final CreateJobUseCase createJobUseCase;
  private final GetJobStatusUseCase getJobStatusUseCase;
  private final DownloadResultUseCase downloadResultUseCase;
  private final ListAllJobsUseCase listAllJobsUseCase;
  private final CancelJobUseCase cancelJobUseCase;
  private final StorageService storageService;
  private final ObjectMapper objectMapper;
  private final RateLimitService rateLimitService;
  private final InputValidationService inputValidationService;
  private final com.pdfprocessor.api.service.SseService sseService;

  public JobController(
      CreateJobUseCase createJobUseCase,
      GetJobStatusUseCase getJobStatusUseCase,
      DownloadResultUseCase downloadResultUseCase,
      ListAllJobsUseCase listAllJobsUseCase,
      CancelJobUseCase cancelJobUseCase,
      StorageService storageService,
      ObjectMapper objectMapper,
      RateLimitService rateLimitService,
      InputValidationService inputValidationService,
      com.pdfprocessor.api.service.SseService sseService) {
    this.createJobUseCase = createJobUseCase;
    this.getJobStatusUseCase = getJobStatusUseCase;
    this.downloadResultUseCase = downloadResultUseCase;
    this.listAllJobsUseCase = listAllJobsUseCase;
    this.cancelJobUseCase = cancelJobUseCase;
    this.storageService = storageService;
    this.objectMapper = objectMapper;
    this.rateLimitService = rateLimitService;
    this.inputValidationService = inputValidationService;
    this.sseService = sseService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Criar novo job de processamento",
      description =
          "Cria um novo job de processamento de PDF. Você pode enviar arquivos via upload ou referenciar arquivos já existentes no sistema.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Job criado com sucesso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                "{\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"PENDING\",\"createdAt\":\"2024-01-15T10:30:00Z\",\"updatedAt\":\"2024-01-15T10:30:00Z\",\"inputFiles\":[\"job-550e8400/input1.pdf\",\"job-550e8400/input2.pdf\"],\"options\":{\"output_filename\":\"merged_document.pdf\"},\"progress\":0}"))),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"error\":\"Bad Request\",\"message\":\"Operation parameter is required\",\"timestamp\":\"2024-01-15T10:30:00Z\"}"))),
        @ApiResponse(
            responseCode = "401",
            description = "Não autorizado - X-API-Key inválida ou ausente"),
        @ApiResponse(responseCode = "413", description = "Arquivo muito grande"),
        @ApiResponse(responseCode = "422", description = "Dados não processáveis")
      })
  public ResponseEntity<JobResponse> createJob(
      @Parameter(description = "Tipo de operação a ser executada", required = true)
          @RequestParam("operation")
          JobOperation operation,
      @Parameter(description = "Arquivos PDF para upload (opcional se inputFiles for fornecido)")
          @RequestPart(value = "files", required = false)
          List<MultipartFile> files,
      @Parameter(description = "Arquivos adicionais para upload (alternativa ao parâmetro files)")
          @RequestPart(value = "inputFiles", required = false)
          List<MultipartFile> inputFiles,
      @Parameter(
              description = "Opções específicas da operação em formato JSON",
              examples = {
                @ExampleObject(
                    name = "merge_options",
                    value = "{\"output_filename\":\"merged.pdf\"}",
                    description = "Opções para merge"),
                @ExampleObject(
                    name = "split_options",
                    value = "{\"pages_per_file\":5,\"output_prefix\":\"page_\"}",
                    description = "Opções para split"),
                @ExampleObject(
                    name = "watermark_options",
                    value = "{\"text\":\"CONFIDENCIAL\",\"opacity\":0.5,\"position\":\"center\"}",
                    description = "Opções para watermark"),
                @ExampleObject(
                    name = "encrypt_options",
                    value =
                        "{\"user_password\":\"1234\",\"owner_password\":\"admin\",\"permissions\":[\"print\"]}",
                    description = "Opções para encrypt")
              })
          @RequestParam(value = "optionsJson", required = false)
          String optionsJson,
      HttpServletRequest httpRequest) {

    try {
      // Verificar rate limit por API key
      String apiKey = httpRequest.getHeader("X-API-Key");
      rateLimitService.checkRateLimit(apiKey);

      System.out.println("DEBUG: API Key = " + apiKey);
      System.out.println("DEBUG: Operation = " + operation);
      System.out.println("DEBUG: Files = " + (files != null ? files.size() : "null"));
      System.out.println(
          "DEBUG: InputFiles = " + (inputFiles != null ? inputFiles.size() : "null"));

      // Validações rigorosas de entrada
      inputValidationService.validateOperation(operation.name());
      inputValidationService.validateUploadedFiles(files);
      if (inputFiles != null && !inputFiles.isEmpty()) {
        inputValidationService.validateUploadedFiles(inputFiles);
      }
      inputValidationService.validateOptionsJson(optionsJson);

      // Validar que pelo menos um tipo de entrada foi fornecido
      if ((files == null || files.isEmpty()) && (inputFiles == null || inputFiles.isEmpty())) {
        throw new IllegalArgumentException(
            "Pelo menos um arquivo deve ser fornecido via 'files' ou 'inputFiles'");
      }

      // Gerar ID único para o job
      String jobId = UUID.randomUUID().toString();

      // Criar diretório do job
      storageService.createJobDirectory(jobId);

      // Determinar lista de arquivos de entrada
      List<String> finalInputFiles = new ArrayList<>();

      if (files != null && !files.isEmpty()) {
        // Caso 1: Upload de arquivos via parâmetro 'files'
        for (MultipartFile file : files) {
          if (!file.isEmpty()) {
            String storedPath =
                storageService.store(jobId, file.getOriginalFilename(), file.getInputStream());
            finalInputFiles.add(storedPath);
          }
        }
      }

      if (inputFiles != null && !inputFiles.isEmpty()) {
        // Caso 2: Upload de arquivos via parâmetro 'inputFiles'
        for (MultipartFile file : inputFiles) {
          if (!file.isEmpty()) {
            String storedPath =
                storageService.store(jobId, file.getOriginalFilename(), file.getInputStream());
            finalInputFiles.add(storedPath);
          }
        }
      }

      // Converter JSON string para Map
      Map<String, Object> options = Map.of();
      if (optionsJson != null && !optionsJson.trim().isEmpty()) {
        try {
          options =
              objectMapper.readValue(optionsJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
          throw new RuntimeException("Invalid options JSON format", e);
        }
      }

      CreateJobRequest request = new CreateJobRequest();
      request.setOperation(operation);
      request.setInputFiles(finalInputFiles);
      request.setOptions(options);
      request.setJobId(jobId); // Passar o jobId gerado

      JobResponse response = createJobUseCase.execute(request);
      return ResponseEntity.ok(response);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store uploaded files", e);
    }
  }

  @GetMapping
  @Operation(
      summary = "Listar todos os jobs",
      description = "Retorna uma lista paginada de todos os jobs")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de jobs retornada com sucesso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                "[{\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"COMPLETED\",\"createdAt\":\"2024-01-15T10:30:00Z\",\"updatedAt\":\"2024-01-15T10:35:00Z\",\"completedAt\":\"2024-01-15T10:35:00Z\",\"inputFiles\":[\"job-550e8400/input1.pdf\",\"job-550e8400/input2.pdf\"],\"outputFiles\":[\"job-550e8400/merged_document.pdf\"],\"options\":{\"output_filename\":\"merged_document.pdf\"},\"progress\":100}]"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
      })
  public ResponseEntity<List<JobResponse>> listAllJobs(
      @Parameter(description = "Número da página (começando em 0)", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Tamanho da página (1-100)", example = "20")
          @RequestParam(value = "size", defaultValue = "20")
          int size) {
    // Validar parâmetros de paginação
    inputValidationService.validatePaginationParams(page, size);

    List<JobResponse> response = listAllJobsUseCase.execute(page, size);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{jobId}")
  @Operation(
      summary = "Obter status do job",
      description = "Retorna o status atual e detalhes de um job específico")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status do job retornado com sucesso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "completed",
                          value =
                              "{\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"COMPLETED\",\"createdAt\":\"2024-01-15T10:30:00Z\",\"updatedAt\":\"2024-01-15T10:35:00Z\",\"completedAt\":\"2024-01-15T10:35:00Z\",\"inputFiles\":[\"job-550e8400/input1.pdf\",\"job-550e8400/input2.pdf\"],\"outputFiles\":[\"job-550e8400/merged_document.pdf\"],\"options\":{\"output_filename\":\"merged_document.pdf\"},\"progress\":100}",
                          description = "Job concluído"),
                      @ExampleObject(
                          name = "processing",
                          value =
                              "{\"jobId\":\"660f9511-f3ac-52e5-b827-557766551111\",\"operation\":\"SPLIT\",\"status\":\"PROCESSING\",\"createdAt\":\"2024-01-15T10:32:00Z\",\"updatedAt\":\"2024-01-15T10:33:00Z\",\"inputFiles\":[\"job-660f9511/document.pdf\"],\"options\":{\"pages_per_file\":5},\"progress\":45}",
                          description = "Job em processamento"),
                      @ExampleObject(
                          name = "failed",
                          value =
                              "{\"jobId\":\"770g0622-g4bd-63f6-c938-668877662222\",\"operation\":\"DECRYPT\",\"status\":\"FAILED\",\"createdAt\":\"2024-01-15T10:40:00Z\",\"updatedAt\":\"2024-01-15T10:41:00Z\",\"inputFiles\":[\"job-770g0622/encrypted.pdf\"],\"options\":{\"password\":\"wrong_password\"},\"progress\":0,\"errorMessage\":\"Invalid password for encrypted PDF\"}",
                          description = "Job com erro")
                    })),
        @ApiResponse(
            responseCode = "404",
            description = "Job não encontrado",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"error\":\"Not Found\",\"message\":\"Job with ID 550e8400-e29b-41d4-a716-446655440000 not found\",\"timestamp\":\"2024-01-15T10:30:00Z\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
      })
  public ResponseEntity<JobResponse> getJobStatus(
      @Parameter(description = "ID único do job", example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          String jobId) {
    // Validar ID do job
    inputValidationService.validateJobId(jobId);

    JobResponse response = getJobStatusUseCase.execute(jobId);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{jobId}/download")
  @Operation(
      summary = "Download do resultado do job",
      description =
          "Faz o download do arquivo resultado de um job concluído. Para operações que geram múltiplos arquivos, retorna um ZIP.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Arquivo resultado retornado com sucesso",
            content = {
              @Content(
                  mediaType = "application/pdf",
                  schema = @Schema(type = "string", format = "binary")),
              @Content(
                  mediaType = "application/zip",
                  schema = @Schema(type = "string", format = "binary")),
              @Content(
                  mediaType = "image/png",
                  schema = @Schema(type = "string", format = "binary")),
              @Content(
                  mediaType = "image/jpeg",
                  schema = @Schema(type = "string", format = "binary")),
              @Content(mediaType = "text/plain", schema = @Schema(type = "string")),
              @Content(mediaType = "application/json", schema = @Schema(type = "object"))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Job não encontrado ou resultado não disponível",
            content =
                @Content(
                    mediaType = "application/json",
                    examples = {
                      @ExampleObject(
                          name = "job_not_found",
                          value =
                              "{\"error\":\"Not Found\",\"message\":\"Job with ID 550e8400-e29b-41d4-a716-446655440000 not found\",\"timestamp\":\"2024-01-15T10:30:00Z\"}",
                          description = "Job não encontrado"),
                      @ExampleObject(
                          name = "result_not_ready",
                          value =
                              "{\"error\":\"Not Found\",\"message\":\"Job result not available. Current status: PROCESSING\",\"timestamp\":\"2024-01-15T10:30:00Z\"}",
                          description = "Resultado não disponível")
                    })),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
      })
  public ResponseEntity<InputStreamResource> downloadResult(
      @Parameter(description = "ID único do job", example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          String jobId) {
    try {
      // Validar ID do job
      inputValidationService.validateJobId(jobId);

      DownloadResultUseCase.DownloadResponse result = downloadResultUseCase.execute(jobId);

      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.getFilename())
          .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.getFileSize()))
          .contentType(MediaType.parseMediaType(result.getContentType()))
          .body(new InputStreamResource(result.getFileStream()));
    } catch (Exception e) {
      throw new RuntimeException("Error downloading job result", e);
    }
  }

  @DeleteMapping("/{jobId}")
  @Operation(summary = "Cancelar job", description = "Cancela um job em execução ou pendente")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Job cancelado com sucesso",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JobResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                "{\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"CANCELLED\",\"createdAt\":\"2024-01-15T10:30:00Z\",\"updatedAt\":\"2024-01-15T10:32:00Z\",\"inputFiles\":[\"job-550e8400/input1.pdf\",\"job-550e8400/input2.pdf\"],\"options\":{\"output_filename\":\"merged_document.pdf\"},\"progress\":25}"))),
        @ApiResponse(responseCode = "404", description = "Job não encontrado"),
        @ApiResponse(
            responseCode = "409",
            description = "Job não pode ser cancelado (já concluído ou falhou)",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"error\":\"Conflict\",\"message\":\"Cannot cancel job in COMPLETED status\",\"timestamp\":\"2024-01-15T10:30:00Z\"}"))),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
      })
  public ResponseEntity<JobResponse> cancelJob(
      @Parameter(description = "ID único do job", example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          String jobId) {
    // Validar ID do job
    inputValidationService.validateJobId(jobId);

    JobResponse response = cancelJobUseCase.execute(jobId);
    return ResponseEntity.ok(response);
  }

  @GetMapping(value = "/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(
      summary = "Acompanhar progresso do job em tempo real",
      description =
          "Estabelece uma conexão Server-Sent Events (SSE) para receber atualizações em tempo real do progresso de um job específico")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conexão SSE estabelecida com sucesso",
            content =
                @Content(
                    mediaType = "text/event-stream",
                    examples = {
                      @ExampleObject(
                          name = "job_update",
                          value =
                              "event: job-update\ndata: {\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"PROCESSING\",\"progress\":45}\n\n",
                          description = "Atualização de progresso"),
                      @ExampleObject(
                          name = "job_finished",
                          value =
                              "event: job-finished\ndata: {\"jobId\":\"550e8400-e29b-41d4-a716-446655440000\",\"operation\":\"MERGE\",\"status\":\"COMPLETED\",\"progress\":100}\n\n",
                          description = "Job concluído")
                    })),
        @ApiResponse(responseCode = "404", description = "Job não encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
      })
  public SseEmitter streamJobEvents(
      @Parameter(description = "ID único do job", example = "550e8400-e29b-41d4-a716-446655440000")
          @PathVariable
          String jobId) {
    // Validar ID do job
    inputValidationService.validateJobId(jobId);

    try {
      // Verificar se o job existe
      getJobStatusUseCase.execute(jobId);

      // Criar e retornar o emitter SSE
      return sseService.createEmitter(jobId);
    } catch (com.pdfprocessor.domain.exception.JobNotFoundException ex) {
      throw ex; // Re-lança para ser tratada pelo GlobalExceptionHandler
    }
  }
}
