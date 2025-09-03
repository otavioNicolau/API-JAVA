package com.pdfprocessor.domain.exception;

/** Exceção lançada quando um job não é encontrado. */
public class JobNotFoundException extends RuntimeException {

  public JobNotFoundException(String jobId) {
    super("Job não encontrado: " + jobId);
  }

  public JobNotFoundException(String jobId, Throwable cause) {
    super("Job não encontrado: " + jobId, cause);
  }
}
