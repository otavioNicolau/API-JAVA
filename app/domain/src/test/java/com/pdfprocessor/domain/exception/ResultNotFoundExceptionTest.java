package com.pdfprocessor.domain.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Testes unitários para ResultNotFoundException. */
class ResultNotFoundExceptionTest {

  @Test
  void shouldCreateExceptionWithResultPath() {
    // Given
    String resultPath = "/path/to/result.pdf";

    // When
    ResultNotFoundException exception = new ResultNotFoundException(resultPath);

    // Then
    assertEquals("Resultado não encontrado: /path/to/result.pdf", exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithResultPathAndCause() {
    // Given
    String resultPath = "/path/to/missing.pdf";
    Throwable cause = new RuntimeException("File system error");

    // When
    ResultNotFoundException exception = new ResultNotFoundException(resultPath, cause);

    // Then
    assertEquals("Resultado não encontrado: /path/to/missing.pdf", exception.getMessage());
    assertEquals(cause, exception.getCause());
  }
}