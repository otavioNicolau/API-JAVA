package com.pdfprocessor.application.usecase;

import com.pdfprocessor.application.dto.CreateJobRequest;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobQueue;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.PdfProcessingService;
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

    if (request.getInputFiles() == null || request.getInputFiles().isEmpty()) {
      throw new IllegalArgumentException("At least one input file is required");
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
