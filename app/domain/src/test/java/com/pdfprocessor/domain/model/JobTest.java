package com.pdfprocessor.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Testes unitários para a entidade Job. */
class JobTest {

  @Test
  void shouldCreateJobWithValidParameters() {
    // Given
    String id = "job-123";
    JobOperation operation = JobOperation.MERGE;
    List<String> inputFiles = List.of("file1.pdf", "file2.pdf");
    Map<String, Object> options = Map.of("quality", "high");

    // When
    Job job = new Job(id, operation, inputFiles, options);

    // Then
    assertEquals(id, job.getId());
    assertEquals(operation, job.getOperation());
    assertEquals(inputFiles, job.getInputFiles());
    assertEquals(options, job.getOptions());
    assertEquals(JobStatus.PENDING, job.getStatus());
    assertEquals(0, job.getProgress());
    assertNotNull(job.getCreatedAt());
    assertNull(job.getErrorMessage());
    assertNull(job.getResultPath());
    assertNull(job.getStartedAt());
    assertNull(job.getCompletedAt());
  }

  @Test
  void shouldCreateJobWithNullOptions() {
    // Given
    String id = "job-123";
    JobOperation operation = JobOperation.SPLIT;
    List<String> inputFiles = List.of("file1.pdf");

    // When
    Job job = new Job(id, operation, inputFiles, null);

    // Then
    assertEquals(Map.of(), job.getOptions());
  }

  @Test
  void shouldThrowExceptionWhenIdIsNull() {
    // Given
    JobOperation operation = JobOperation.MERGE;
    List<String> inputFiles = List.of("file1.pdf");
    Map<String, Object> options = Map.of();

    // When & Then
    assertThrows(
        NullPointerException.class, () -> new Job(null, operation, inputFiles, options));
  }

  @Test
  void shouldThrowExceptionWhenOperationIsNull() {
    // Given
    String id = "job-123";
    List<String> inputFiles = List.of("file1.pdf");
    Map<String, Object> options = Map.of();

    // When & Then
    assertThrows(NullPointerException.class, () -> new Job(id, null, inputFiles, options));
  }

  @Test
  void shouldThrowExceptionWhenInputFilesIsNull() {
    // Given
    String id = "job-123";
    JobOperation operation = JobOperation.MERGE;
    Map<String, Object> options = Map.of();

    // When & Then
    assertThrows(NullPointerException.class, () -> new Job(id, operation, null, options));
  }

  @Test
  void shouldStartJob() {
    // Given
    Job job = createValidJob();

    // When
    job.start();

    // Then
    assertEquals(JobStatus.PROCESSING, job.getStatus());
    assertNotNull(job.getStartedAt());
  }

  @Test
  void shouldUpdateProgress() {
    // Given
    Job job = createValidJob();
    job.start();
    int newProgress = 50;

    // When
    job.updateProgress(newProgress);

    // Then
    assertEquals(newProgress, job.getProgress());
  }

  @Test
  void shouldCompleteJob() {
    // Given
    Job job = createValidJob();
    job.start();
    String resultPath = "/path/to/result.pdf";

    // When
    job.complete(resultPath);

    // Then
    assertEquals(JobStatus.COMPLETED, job.getStatus());
    assertEquals(resultPath, job.getResultPath());
    assertEquals(100, job.getProgress());
    assertNotNull(job.getCompletedAt());
    assertTrue(job.isCompleted());
  }

  @Test
  void shouldFailJob() {
    // Given
    Job job = createValidJob();
    job.start();
    String errorMessage = "Processing failed";

    // When
    job.fail(errorMessage);

    // Then
    assertEquals(JobStatus.FAILED, job.getStatus());
    assertEquals(errorMessage, job.getErrorMessage());
    assertNotNull(job.getCompletedAt());
    assertTrue(job.isFailed());
  }

  @Test
  void shouldCancelJob() {
    // Given
    Job job = createValidJob();

    // When
    job.cancel();

    // Then
    assertEquals(JobStatus.CANCELLED, job.getStatus());
    assertNotNull(job.getCompletedAt());
  }

  @Test
  void shouldReturnTrueForIsProcessing() {
    // Given
    Job job = createValidJob();
    job.start();

    // When & Then
    assertTrue(job.isProcessing());
  }

  @Test
  void shouldReturnFalseForIsProcessingWhenNotProcessing() {
    // Given
    Job job = createValidJob();

    // When & Then
    assertFalse(job.isProcessing());
  }

  @Test
  void shouldImplementEqualsCorrectly() {
    // Given
    Job job1 = createValidJob();
    Job job2 = createValidJob();
    Job job3 = new Job("different-id", JobOperation.MERGE, List.of("file1.pdf"), Map.of());

    // When & Then
    assertEquals(job1, job2);
    assertNotEquals(job1, job3);
    assertNotEquals(job1, null);
    assertNotEquals(job1, "not a job");
  }

  @Test
  void shouldImplementHashCodeCorrectly() {
    // Given
    Job job1 = createValidJob();
    Job job2 = createValidJob();

    // When & Then
    assertEquals(job1.hashCode(), job2.hashCode());
  }

  @Test
  void shouldImplementToStringCorrectly() {
    // Given
    Job job = createValidJob();

    // When
    String toString = job.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("job-123"));
    assertTrue(toString.contains("MERGE"));
  }

  private Job createValidJob() {
    return new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
  }
}