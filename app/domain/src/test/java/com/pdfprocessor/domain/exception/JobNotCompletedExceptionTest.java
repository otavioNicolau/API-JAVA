package com.pdfprocessor.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes unitários para JobNotCompletedException. */
class JobNotCompletedExceptionTest {

  @Test
  void shouldCreateExceptionWithJobId() {
    // Given
    String jobId = "job-123";

    // When
    JobNotCompletedException exception = new JobNotCompletedException(jobId);

    // Then
    assertEquals("Job não foi completado: job-123", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithJobIdAndCause() {
    // Given
    String jobId = "job-456";
    Throwable cause = new RuntimeException("Processing error");

    // When
    JobNotCompletedException exception = new JobNotCompletedException(jobId, cause);

    // Then
    assertEquals("Job não foi completado: job-456", exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}