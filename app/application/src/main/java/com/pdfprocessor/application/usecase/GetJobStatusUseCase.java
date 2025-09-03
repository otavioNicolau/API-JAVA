package com.pdfprocessor.application.usecase;

import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.exception.JobNotFoundException;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Caso de uso para obter o status de um job. */
@Service
public class GetJobStatusUseCase {

  private final JobRepository jobRepository;

  public GetJobStatusUseCase(JobRepository jobRepository) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
  }

  /**
   * Executa o caso de uso de obtenção de status do job.
   *
   * @param jobId ID do job
   * @return resposta com dados do job
   * @throws JobNotFoundException se o job não for encontrado
   */
  public JobResponse execute(String jobId) {
    if (jobId == null || jobId.trim().isEmpty()) {
      throw new IllegalArgumentException("Job ID cannot be null or empty");
    }

    Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));

    return JobResponse.fromJob(job);
  }
}
