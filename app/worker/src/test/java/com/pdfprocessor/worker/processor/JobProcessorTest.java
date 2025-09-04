package com.pdfprocessor.worker.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.PdfProcessingService;
import com.pdfprocessor.domain.port.StorageService;
import java.util.Arrays;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobProcessorTest {

  @Mock private JobRepository jobRepository;

  @Mock private StorageService storageService;

  @Mock private PdfProcessingService pdfProcessingService;

  private JobProcessor jobProcessor;

  @BeforeEach
  void setUp() {
    jobProcessor = new JobProcessor(jobRepository, storageService, pdfProcessingService);
  }

  @Test
  void shouldProcessJobSuccessfully() {
    // Given
    Job job = createTestJob();
    String expectedResultPath = "/path/to/result.pdf";
    when(pdfProcessingService.processJob(job)).thenReturn(expectedResultPath);

    // When
    jobProcessor.process(job);

    // Then
    assertEquals(JobStatus.COMPLETED, job.getStatus());
    assertEquals(expectedResultPath, job.getResultPath());
    assertNotNull(job.getCompletedAt());
    
    verify(jobRepository, times(2)).save(job); // Once for PROCESSING, once for COMPLETED
    verify(pdfProcessingService).processJob(job);
  }

  @Test
  void shouldHandleProcessingFailure() {
    // Given
    Job job = createTestJob();
    String errorMessage = "Processing failed";
    RuntimeException exception = new RuntimeException(errorMessage);
    when(pdfProcessingService.processJob(job)).thenThrow(exception);

    // When & Then
    RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
      jobProcessor.process(job);
    });

    assertEquals("Falha no processamento do job: " + job.getId(), thrownException.getMessage());
    assertEquals(JobStatus.FAILED, job.getStatus());
    assertEquals(errorMessage, job.getErrorMessage());
    
    verify(jobRepository, times(2)).save(job); // Once for PROCESSING, once for FAILED
    verify(pdfProcessingService).processJob(job);
  }

  @Test
  void shouldUpdateJobStatusToProcessingBeforeProcessing() {
    // Given
    Job job = createTestJob();
    String expectedResultPath = "/path/to/result.pdf";
    when(pdfProcessingService.processJob(job)).thenReturn(expectedResultPath);

    // When
    jobProcessor.process(job);

    // Then
    verify(jobRepository, times(2)).save(job);
    assertEquals(JobStatus.COMPLETED, job.getStatus());
    assertNotNull(job.getStartedAt());
    assertNotNull(job.getCompletedAt());
  }

  @Test
  void shouldSaveJobAfterStatusChanges() {
    // Given
    Job job = createTestJob();
    String expectedResultPath = "/path/to/result.pdf";
    when(pdfProcessingService.processJob(job)).thenReturn(expectedResultPath);

    // When
    jobProcessor.process(job);

    // Then
    verify(jobRepository, times(2)).save(job);
  }

  @Test
  void shouldCallPdfProcessingServiceWithCorrectJob() {
    // Given
    Job job = createTestJob();
    String expectedResultPath = "/path/to/result.pdf";
    when(pdfProcessingService.processJob(job)).thenReturn(expectedResultPath);

    // When
    jobProcessor.process(job);

    // Then
    verify(pdfProcessingService).processJob(job);
  }

  private Job createTestJob() {
    return new Job(
        "test-job-123",
        JobOperation.MERGE,
        Arrays.asList("file1.pdf", "file2.pdf"),
        new HashMap<>());
  }
}