package com.pdfprocessor.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.pdfprocessor.application.dto.CreateJobRequest;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.JobQueue;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.PdfProcessingService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Testes unitários para CreateJobUseCase. */
@ExtendWith(MockitoExtension.class)
class CreateJobUseCaseTest {

  @Mock private JobRepository jobRepository;
  @Mock private JobQueue jobQueue;
  @Mock private PdfProcessingService pdfProcessingService;

  private CreateJobUseCase createJobUseCase;

  @BeforeEach
  void setUp() {
    createJobUseCase = new CreateJobUseCase(jobRepository, jobQueue, pdfProcessingService);
  }

  @Test
  void shouldCreateJobSuccessfully() {
    // Given
    CreateJobRequest request =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
    request.setJobId("job-123");

    Job savedJob =
        new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());

    when(pdfProcessingService.supportsOperation(JobOperation.MERGE)).thenReturn(true);
    when(pdfProcessingService.validateOptions(eq(JobOperation.MERGE), any())).thenReturn(true);
    when(pdfProcessingService.getSupportedOperations()).thenReturn(List.of(JobOperation.MERGE));
    when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

    // When
    JobResponse response = createJobUseCase.execute(request);

    // Then
    assertNotNull(response);
    assertEquals("job-123", response.getId());
    assertEquals(JobOperation.MERGE, response.getOperation());
    assertEquals(List.of("file1.pdf", "file2.pdf"), response.getInputFiles());

    verify(jobRepository).save(any(Job.class));
    verify(jobQueue).publish(savedJob);
  }

  @Test
  void shouldGenerateJobIdWhenNotProvided() {
    // Given
    CreateJobRequest request =
        new CreateJobRequest(JobOperation.SPLIT, List.of("file1.pdf"), Map.of());
    // jobId não definido

    when(pdfProcessingService.supportsOperation(JobOperation.SPLIT)).thenReturn(true);
    when(pdfProcessingService.validateOptions(eq(JobOperation.SPLIT), any())).thenReturn(true);
    when(pdfProcessingService.getSupportedOperations()).thenReturn(List.of(JobOperation.SPLIT));
    when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    JobResponse response = createJobUseCase.execute(request);

    // Then
    assertNotNull(response);
    assertNotNull(response.getId());
    assertFalse(response.getId().isEmpty());
  }

  @Test
  void shouldThrowExceptionWhenRequestIsNull() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(null));
    assertEquals("Request cannot be null", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenOperationIsNull() {
    // Given
    CreateJobRequest request = new CreateJobRequest(null, List.of("file1.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(request));
    assertEquals("Operation is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenInputFilesIsNull() {
    // Given
    CreateJobRequest request = new CreateJobRequest(JobOperation.MERGE, null, Map.of());

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(request));
    assertEquals("At least one input file is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenInputFilesIsEmpty() {
    // Given
    CreateJobRequest request = new CreateJobRequest(JobOperation.MERGE, List.of(), Map.of());

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(request));
    assertEquals("At least one input file is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenOperationNotSupported() {
    // Given
    CreateJobRequest request =
        new CreateJobRequest(JobOperation.ROTATE, List.of("file1.pdf"), Map.of());

    when(pdfProcessingService.supportsOperation(JobOperation.ROTATE)).thenReturn(false);
    when(pdfProcessingService.getSupportedOperations()).thenReturn(List.of(JobOperation.MERGE));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(request));
    assertEquals("Operation not supported: ROTATE", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenOptionsAreInvalid() {
    // Given
    CreateJobRequest request =
        new CreateJobRequest(JobOperation.MERGE, List.of("file1.pdf"), Map.of("invalid", "option"));

    when(pdfProcessingService.supportsOperation(JobOperation.MERGE)).thenReturn(true);
    when(pdfProcessingService.validateOptions(eq(JobOperation.MERGE), any())).thenReturn(false);
    when(pdfProcessingService.getSupportedOperations()).thenReturn(List.of(JobOperation.MERGE));

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> createJobUseCase.execute(request));
    assertEquals("Invalid options for operation: MERGE", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenJobRepositoryIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> new CreateJobUseCase(null, jobQueue, pdfProcessingService));
  }

  @Test
  void shouldThrowExceptionWhenJobQueueIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class,
        () -> new CreateJobUseCase(jobRepository, null, pdfProcessingService));
  }

  @Test
  void shouldThrowExceptionWhenPdfProcessingServiceIsNull() {
    // When & Then
    assertThrows(
        NullPointerException.class, () -> new CreateJobUseCase(jobRepository, jobQueue, null));
  }
}
