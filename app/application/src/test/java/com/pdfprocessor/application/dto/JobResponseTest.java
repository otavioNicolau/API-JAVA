package com.pdfprocessor.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Testes unitários para JobResponse. */
class JobResponseTest {

  @Test
  void shouldCreateResponseFromJob() {
    // Given
    Job job = new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
    job.start();
    job.updateProgress(50);

    // When
    JobResponse response = JobResponse.fromJob(job);

    // Then
    assertEquals("job-123", response.getId());
    assertEquals(JobOperation.MERGE, response.getOperation());
    assertEquals(JobStatus.PROCESSING, response.getStatus());
    assertEquals(List.of("file1.pdf", "file2.pdf"), response.getInputFiles());
    assertEquals(Map.of(), response.getOptions());
    assertEquals(50, response.getProgress());
    assertNotNull(response.getCreatedAt());
    assertNotNull(response.getStartedAt());
    assertNull(response.getCompletedAt());
    assertNull(response.getErrorMessage());
    assertNull(response.getResultPath());
  }

  @Test
  void shouldCreateResponseFromCompletedJob() {
    // Given
    Job job = new Job("job-456", JobOperation.SPLIT, List.of("file1.pdf"), Map.of("pages", "1-5"));
    job.start();
    job.complete("/path/to/result.pdf");

    // When
    JobResponse response = JobResponse.fromJob(job);

    // Then
    assertEquals("job-456", response.getId());
    assertEquals(JobOperation.SPLIT, response.getOperation());
    assertEquals(JobStatus.COMPLETED, response.getStatus());
    assertEquals("/path/to/result.pdf", response.getResultPath());
    assertEquals(100, response.getProgress());
    assertNotNull(response.getCompletedAt());
  }

  @Test
  void shouldCreateResponseFromFailedJob() {
    // Given
    Job job = new Job("job-789", JobOperation.ROTATE, List.of("file1.pdf"), Map.of());
    job.start();
    job.fail("Processing error");

    // When
    JobResponse response = JobResponse.fromJob(job);

    // Then
    assertEquals("job-789", response.getId());
    assertEquals(JobStatus.FAILED, response.getStatus());
    assertEquals("Processing error", response.getErrorMessage());
    assertNotNull(response.getCompletedAt());
  }

  @Test
  void shouldCreateEmptyResponse() {
    // When
    JobResponse response = new JobResponse();

    // Then
    assertNull(response.getId());
    assertNull(response.getOperation());
    assertNull(response.getStatus());
    assertNull(response.getInputFiles());
    assertNull(response.getOptions());
    assertNull(response.getErrorMessage());
    assertNull(response.getResultPath());
    assertNull(response.getProgress());
    assertNull(response.getCreatedAt());
    assertNull(response.getStartedAt());
    assertNull(response.getCompletedAt());
  }

  @Test
  void shouldSetAndGetAllFields() {
    // Given
    JobResponse response = new JobResponse();
    LocalDateTime now = LocalDateTime.now();

    // When
    response.setId("job-123");
    response.setOperation(JobOperation.MERGE);
    response.setStatus(JobStatus.PROCESSING);
    response.setInputFiles(List.of("file1.pdf"));
    response.setOptions(Map.of("key", "value"));
    response.setErrorMessage("error");
    response.setResultPath("/path/result.pdf");
    response.setProgress(75);
    response.setCreatedAt(now);
    response.setStartedAt(now);
    response.setCompletedAt(now);

    // Then
    assertEquals("job-123", response.getId());
    assertEquals(JobOperation.MERGE, response.getOperation());
    assertEquals(JobStatus.PROCESSING, response.getStatus());
    assertEquals(List.of("file1.pdf"), response.getInputFiles());
    assertEquals(Map.of("key", "value"), response.getOptions());
    assertEquals("error", response.getErrorMessage());
    assertEquals("/path/result.pdf", response.getResultPath());
    assertEquals(75, response.getProgress());
    assertEquals(now, response.getCreatedAt());
    assertEquals(now, response.getStartedAt());
    assertEquals(now, response.getCompletedAt());
  }

  @Test
  void shouldImplementEqualsCorrectly() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    JobResponse response1 =
        new JobResponse(
            "job-123",
            JobOperation.MERGE,
            JobStatus.PENDING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            0,
            now,
            null,
            null);
    JobResponse response2 =
        new JobResponse(
            "job-123",
            JobOperation.MERGE,
            JobStatus.PENDING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            0,
            now,
            null,
            null);
    JobResponse response3 =
        new JobResponse(
            "job-456",
            JobOperation.SPLIT,
            JobStatus.PENDING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            0,
            now,
            null,
            null);

    // When & Then
    assertEquals(response1, response2);
    assertNotEquals(response1, response3);
    assertNotEquals(response1, null);
    assertNotEquals(response1, "not a response");
  }

  @Test
  void shouldImplementHashCodeCorrectly() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    JobResponse response1 =
        new JobResponse(
            "job-123",
            JobOperation.MERGE,
            JobStatus.PENDING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            0,
            now,
            null,
            null);
    JobResponse response2 =
        new JobResponse(
            "job-123",
            JobOperation.MERGE,
            JobStatus.PENDING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            0,
            now,
            null,
            null);

    // When & Then
    assertEquals(response1.hashCode(), response2.hashCode());
  }

  @Test
  void shouldImplementToStringCorrectly() {
    // Given
    JobResponse response =
        new JobResponse(
            "job-123",
            JobOperation.MERGE,
            JobStatus.PROCESSING,
            List.of("file1.pdf"),
            Map.of(),
            null,
            null,
            50,
            LocalDateTime.now(),
            null,
            null);

    // When
    String toString = response.toString();

    // Then
    assertNotNull(toString);
    assertTrue(toString.contains("job-123"));
    assertTrue(toString.contains("MERGE"));
    assertTrue(toString.contains("PROCESSING"));
  }
}
