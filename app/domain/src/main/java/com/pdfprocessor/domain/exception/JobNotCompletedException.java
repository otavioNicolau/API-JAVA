package com.pdfprocessor.domain.exception;

/** Exceção lançada quando se tenta acessar o resultado de um job que não foi completado. */
public class JobNotCompletedException extends RuntimeException {

  public JobNotCompletedException(String jobId) {
    super("Job não foi completado: " + jobId);
  }

  public JobNotCompletedException(String jobId, Throwable cause) {
    super("Job não foi completado: " + jobId, cause);
  }
}
