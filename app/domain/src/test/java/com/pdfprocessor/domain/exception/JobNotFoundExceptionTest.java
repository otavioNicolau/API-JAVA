package com.pdfprocessor.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes unitários para JobNotFoundException. */
class JobNotFoundExceptionTest {

  @Test
  void shouldCreateExceptionWithJobId() {
    // Given
    String jobId = "job-123";

    // When
    JobNotFoundException exception = new JobNotFoundException(jobId);

    // Then
    assertEquals("Job não encontrado: job-123", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithJobIdAndCause() {
    // Given
    String jobId = "job-456";
    Throwable cause = new RuntimeException("Database error");

    // When
    JobNotFoundException exception = new JobNotFoundException(jobId, cause);

    // Then
    assertEquals("Job não encontrado: job-456", exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}