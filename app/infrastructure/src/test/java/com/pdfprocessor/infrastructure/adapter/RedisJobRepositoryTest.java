package com.pdfprocessor.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

/** Testes unitários para RedisJobRepository. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisJobRepositoryTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;
  @Mock private SetOperations<String, Object> setOperations;

  private RedisJobRepository repository;
  private Job testJob;

  @BeforeEach
  void setUp() {
    repository = new RedisJobRepository(redisTemplate);
    testJob = new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
  }

  @Test
  void shouldSaveJobSuccessfully() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);

    // When
    Job savedJob = repository.save(testJob);

    // Then
    assertNotNull(savedJob);
    assertEquals(testJob.getId(), savedJob.getId());
  }

  @Test
  void shouldFindJobByIdSuccessfully() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    String jobJson =
        "{\"id\":\"job-123\",\"operation\":\"MERGE\",\"inputFiles\":[\"file1.pdf\",\"file2.pdf\"],\"options\":{},\"status\":\"PENDING\",\"createdAt\":\"2023-01-01T10:00:00\",\"updatedAt\":\"2023-01-01T10:00:00\"}";
    when(valueOperations.get("pdf:job:job-123")).thenReturn(jobJson);

    // When
    Optional<Job> foundJob = repository.findById("job-123");

    // Then
    assertTrue(foundJob.isPresent());
    assertEquals("job-123", foundJob.get().getId());
    assertEquals(JobOperation.MERGE, foundJob.get().getOperation());
  }

  @Test
  void shouldReturnEmptyWhenJobNotFound() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("pdf:job:nonexistent")).thenReturn(null);

    // When
    Optional<Job> foundJob = repository.findById("nonexistent");

    // Then
    assertFalse(foundJob.isPresent());
  }

  @Test
  void shouldFindAllJobsWithPagination() {
    // Given
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Set<Object> jobIds = Set.of("job-1", "job-2");
    when(setOperations.members("pdf:jobs:index")).thenReturn(jobIds);

    String job1Json =
        "{\"id\":\"job-1\",\"operation\":\"MERGE\",\"inputFiles\":[\"file1.pdf\"],\"options\":{},\"status\":\"PENDING\",\"createdAt\":\"2023-01-01T10:00:00\",\"updatedAt\":\"2023-01-01T10:00:00\"}";
    String job2Json =
        "{\"id\":\"job-2\",\"operation\":\"SPLIT\",\"inputFiles\":[\"file2.pdf\"],\"options\":{},\"status\":\"PENDING\",\"createdAt\":\"2023-01-01T10:00:00\",\"updatedAt\":\"2023-01-01T10:00:00\"}";

    when(valueOperations.get("pdf:job:job-1")).thenReturn(job1Json);
    when(valueOperations.get("pdf:job:job-2")).thenReturn(job2Json);

    // When
    List<Job> jobs = repository.findAll(0, 2);

    // Then
    assertEquals(2, jobs.size());
  }

  @Test
  void shouldReturnEmptyListWhenNoJobsExist() {
    // Given
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(setOperations.members("pdf:jobs:index")).thenReturn(Set.of());

    // When
    List<Job> jobs = repository.findAll(0, 10);

    // Then
    assertTrue(jobs.isEmpty());
  }

  @Test
  void shouldDeleteJobSuccessfully() {
    // When
    repository.deleteById("job-123");

    // Then
    verify(redisTemplate).delete("pdf:job:job-123");
  }

  @Test
  void shouldCheckIfJobExists() {
    // Given
    when(redisTemplate.hasKey("pdf:job:job-123")).thenReturn(true);

    // When
    boolean exists = repository.existsById("job-123");

    // Then
    assertTrue(exists);
  }

  @Test
  void shouldReturnFalseWhenJobDoesNotExist() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get("pdf:job:nonexistent")).thenReturn(null);

    // When
    boolean exists = repository.existsById("nonexistent");

    // Then
    assertFalse(exists);
  }
}
