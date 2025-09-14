package com.pdfprocessor.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Testes unitários para CreateJobRequest. */
class CreateJobRequestTest {

  @Test
  void shouldCreateRequestWithAllParameters() {
    // Given
    JobOperation operation = JobOperation.MERGE;
    List<String> inputFiles = List.of("file1.pdf", "file2.pdf");
    Map<String, Object> options = Map.of("quality", "high");

    // When
    CreateJobRequest request = new CreateJobRequest(operation, inputFiles, options);

    // Then
    assertEquals(operation, request.getOperation());
    assertEquals(inputFiles, request.getInputFiles());
    assertEquals(options, request.getOptions());
    assertNull(request.getJobId());
  }

  @Test
  void shouldCreateEmptyRequest() {
    // When
    CreateJobRequest request = new CreateJobRequest();

    // Then
    assertNull(request.getOperation());
    assertNull(request.getInputFiles());
    assertNull(request.getOptions());
    assertNull(request.getJobId());
  }

  @Test
  void shouldSetAndGetJobId() {
    // Given
    CreateJobRequest request = new CreateJobRequest();
    String jobId = "job-123";

    // When
    request.setJobId(jobId);

    // Then
    assertEquals(jobId, request.getJobId());
  }

  @Test
  void shouldSetAndGetOperation() {
    // Given
    CreateJobRequest request = new CreateJobRequest();
    JobOperation operation = JobOperation.SPLIT;

    // When
    request.setOperation(operation);

    // Then
    assertEquals(operation, request.getOperation());
  }

  @Test
  void shouldSetAndGetInputFiles() {
    // Given
    CreateJobRequest request = new CreateJobRequest();
    List<String> inputFiles = List.of("file1.pdf");

    // When
    request.setInputFiles(inputFiles);

    // Then
    assertEquals(inputFiles, request.getInputFiles());
  }

  @Test
  void shouldSetAndGetOptions() {
    // Given
    CreateJobRequest request = new CreateJobRequest();
    Map<String, Object> options = Map.of("pages", "1-5");

    // When
    request.setOptions(options);

    // Then
    assertEquals(options, request.getOptions());
  }

  @Test
  void shouldImplementEqualsCorrectly() {
    // Given
    CreateJobRequest request1 =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of());
    CreateJobRequest request2 =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of());
    CreateJobRequest request3 =
        new CreateJobRequest(JobOperation.SPLIT, List.of("file1.pdf"), Map.of());

    // When & Then
    assertEquals(request1, request2);
    assertNotEquals(request1, request3);
    assertNotEquals(request1, null);
    assertNotEquals(request1, "not a request");
  }

  @Test
  void shouldImplementHashCodeCorrectly() {
    // Given
    CreateJobRequest request1 =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of());
    CreateJobRequest request2 =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of());

    // When & Then
    assertEquals(request1.hashCode(), request2.hashCode());
  }

  @Test
  void shouldImplementToStringCorrectly() {
    // Given
    CreateJobRequest request =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of("key", "value"));

    // When
    String toString = request.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("MERGE"));
    assertTrue(toString.contains("file1.pdf"));
  }
}
