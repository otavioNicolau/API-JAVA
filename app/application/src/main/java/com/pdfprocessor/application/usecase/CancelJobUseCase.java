package com.pdfprocessor.application.usecase;

import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.exception.JobNotFoundException;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Caso de uso para cancelar um job. */
@Service
public class CancelJobUseCase {

  private final JobRepository jobRepository;

  public CancelJobUseCase(JobRepository jobRepository) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
  }

  /**
   * Executa o caso de uso de cancelamento de job.
   *
   * @param jobId ID do job
   * @return resposta com dados do job cancelado
   * @throws JobNotFoundException se o job não for encontrado
   * @throws IllegalStateException se o job não puder ser cancelado
   */
  public JobResponse execute(String jobId) {
    if (jobId == null || jobId.trim().isEmpty()) {
      throw new IllegalArgumentException("Job ID cannot be null or empty");
    }

    Job job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));

    job.cancel();
    Job savedJob = jobRepository.save(job);

    return JobResponse.fromJob(savedJob);
  }
}
