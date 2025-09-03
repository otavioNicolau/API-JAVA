package com.pdfprocessor.application.usecase;

import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Caso de uso para listar todos os jobs. */
@Service
public class ListAllJobsUseCase {

  private final JobRepository jobRepository;

  public ListAllJobsUseCase(JobRepository jobRepository) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
  }

  /**
   * Executa o caso de uso de listagem de jobs.
   *
   * @param page número da página (0-based)
   * @param size tamanho da página
   * @return lista de jobs
   */
  public List<JobResponse> execute(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page must be non-negative");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("Size must be positive");
    }

    List<Job> jobs = jobRepository.findAll(page, size);
    return jobs.stream().map(JobResponse::fromJob).toList();
  }

  /**
   * Executa o caso de uso de listagem de jobs com valores padrão.
   *
   * @return lista de jobs (primeira página, 20 itens)
   */
  public List<JobResponse> execute() {
    return execute(0, 20);
  }
}
