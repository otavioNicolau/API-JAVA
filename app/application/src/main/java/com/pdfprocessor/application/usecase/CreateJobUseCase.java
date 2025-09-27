package com.pdfprocessor.application.usecase;

import com.pdfprocessor.application.dto.CreateJobRequest;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.PdfProcessingService;
import com.pdfprocessor.domain.port.JobQueue;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Caso de uso para criar um novo job de processamento. */
@Service
public class CreateJobUseCase {

  private final JobRepository jobRepository;
  private final JobQueue jobQueue;
  private final PdfProcessingService pdfProcessingService;

  public CreateJobUseCase(
      JobRepository jobRepository, JobQueue jobQueue, PdfProcessingService pdfProcessingService) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
    this.jobQueue = Objects.requireNonNull(jobQueue);
    this.pdfProcessingService = Objects.requireNonNull(pdfProcessingService);
  }

  /**
   * Executa o caso de uso de criação de job.
   *
   * @param request dados do job a ser criado
   * @return resposta com dados do job criado
   */
  public JobResponse execute(CreateJobRequest request) {
    System.out.println("DEBUG: CreateJobUseCase.execute() called with request: " + request);
    System.out.println("DEBUG: Request operation: " + (request != null ? request.getOperation() : "null"));
    System.out.println("DEBUG: Request inputFiles: " + (request != null ? request.getInputFiles() : "null"));
    
    validateRequest(request);

    // Usa o jobId fornecido pelo request (já gerado no controller)
    String jobId = request.getJobId() != null ? request.getJobId() : UUID.randomUUID().toString();

    // Cria o job
    Job job = new Job(jobId, request.getOperation(), request.getInputFiles(), request.getOptions());

    // Salva o job
    Job savedJob = jobRepository.save(job);

    // Publica na fila para processamento assíncrono
    jobQueue.publish(savedJob);

    return JobResponse.fromJob(savedJob);
  }

  private void validateRequest(CreateJobRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Request cannot be null");
    }

    if (request.getOperation() == null) {
      throw new IllegalArgumentException("Operation is required");
    }

    System.out.println("DEBUG: UseCase validation - Operation: " + request.getOperation());
    System.out.println("DEBUG: UseCase validation - Is PDF_CREATE: " + request.getOperation().equals(JobOperation.PDF_CREATE));
    System.out.println("DEBUG: UseCase validation - InputFiles: " + request.getInputFiles());
    System.out.println("DEBUG: UseCase validation - InputFiles null: " + (request.getInputFiles() == null));
    System.out.println("DEBUG: UseCase validation - InputFiles empty: " + (request.getInputFiles() != null && request.getInputFiles().isEmpty()));

    // Validação de arquivos de entrada removida - será feita no controller
    
    // Para PDF_CREATE, garantir lista vazia de arquivos
    if (request.getOperation().equals(JobOperation.PDF_CREATE) && request.getInputFiles() == null) {
      request.setInputFiles(new ArrayList<>());
    }

    // Debug: verificar instância do serviço e operações suportadas
    System.out.println(
        "DEBUG: PdfProcessingService instance: " + pdfProcessingService.getClass().getName());
    System.out.println(
        "DEBUG: Supported operations: " + pdfProcessingService.getSupportedOperations());
    System.out.println("DEBUG: Checking operation: " + request.getOperation());
    System.out.println(
        "DEBUG: Operation supported: "
            + pdfProcessingService.supportsOperation(request.getOperation()));

    // Valida se a operação é suportada
    if (!pdfProcessingService.supportsOperation(request.getOperation())) {
      throw new IllegalArgumentException("Operation not supported: " + request.getOperation());
    }

    // Valida as opções
    if (!pdfProcessingService.validateOptions(request.getOperation(), request.getOptions())) {
      throw new IllegalArgumentException(
          "Invalid options for operation: " + request.getOperation());
    }
  }
}
