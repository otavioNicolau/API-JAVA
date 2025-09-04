package com.pdfprocessor.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.domain.exception.JobNotFoundException;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.JobRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Testes unitários para GetJobStatusUseCase. */
@ExtendWith(MockitoExtension.class)
class GetJobStatusUseCaseTest {

  @Mock private JobRepository jobRepository;

  private GetJobStatusUseCase getJobStatusUseCase;

  @BeforeEach
  void setUp() {
    getJobStatusUseCase = new GetJobStatusUseCase(jobRepository);
  }

  @Test
  void shouldReturnJobStatusWhenJobExists() {
    // Given
    String jobId = "job-123";
    Job job = new Job(jobId, JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
    when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

    // When
    JobResponse response = getJobStatusUseCase.execute(jobId);

    // Then
    assertNotNull(response);
    assertEquals(jobId, response.getId());
    assertEquals(JobOperation.MERGE, response.getOperation());
    assertEquals(List.of("file1.pdf", "file2.pdf"), response.getInputFiles());
    verify(jobRepository).findById(jobId);
  }

  @Test
  void shouldThrowJobNotFoundExceptionWhenJobDoesNotExist() {
    // Given
    String jobId = "non-existent-job";
    when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

    // When & Then
    JobNotFoundException exception =
        assertThrows(JobNotFoundException.class, () -> getJobStatusUseCase.execute(jobId));
    assertTrue(exception.getMessage().contains(jobId));
    verify(jobRepository).findById(jobId);
  }

  @Test
  void shouldThrowExceptionWhenJobIdIsNull() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> getJobStatusUseCase.execute(null));
    assertEquals("Job ID cannot be null or empty", exception.getMessage());
    verifyNoInteractions(jobRepository);
  }

  @Test
  void shouldThrowExceptionWhenJobIdIsEmpty() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> getJobStatusUseCase.execute(""));
    assertEquals("Job ID cannot be null or empty", exception.getMessage());
    verifyNoInteractions(jobRepository);
  }

  @Test
  void shouldThrowExceptionWhenJobIdIsBlank() {
    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> getJobStatusUseCase.execute("   "));
    assertEquals("Job ID cannot be null or empty", exception.getMessage());
    verifyNoInteractions(jobRepository);
  }

  @Test
  void shouldThrowExceptionWhenJobRepositoryIsNull() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new GetJobStatusUseCase(null));
  }
}