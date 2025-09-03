package com.pdfprocessor.worker.processor;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.PdfProcessingService;
import com.pdfprocessor.domain.port.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Processador de jobs responsável por executar as operações de PDF. */
@Component
public class JobProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobProcessor.class);

  private final JobRepository jobRepository;
  private final StorageService storageService;
  private final PdfProcessingService pdfProcessingService;

  @Autowired
  public JobProcessor(
      JobRepository jobRepository,
      StorageService storageService,
      PdfProcessingService pdfProcessingService) {
    this.jobRepository = jobRepository;
    this.storageService = storageService;
    this.pdfProcessingService = pdfProcessingService;
  }

  /**
   * Processa um job de PDF.
   *
   * @param job o job a ser processado
   */
  public void process(Job job) {
    LOGGER.info(
        "Iniciando processamento do job: {} - Operação: {}", job.getId(), job.getOperation());

    try {
      // Atualiza status para PROCESSING
      job.start();
      jobRepository.save(job);

      // Processa o job usando o PdfProcessingService
      String resultPath = pdfProcessingService.processJob(job);

      LOGGER.info("Arquivo resultado gerado em: {}", resultPath);

      // Atualiza status para COMPLETED
      job.complete(resultPath);
      jobRepository.save(job);

      LOGGER.info("Job processado com sucesso: {}", job.getId());

    } catch (Exception e) {
      LOGGER.error("Erro ao processar job {}: {}", job.getId(), e.getMessage(), e);

      // Atualiza status para FAILED
      job.fail(e.getMessage());
      jobRepository.save(job);

      throw new RuntimeException("Falha no processamento do job: " + job.getId(), e);
    }
  }
}
