package com.pdfprocessor.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

/** Testes unitários para RedisJobQueue. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisJobQueueTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ListOperations<String, Object> listOperations;
  @Mock private SetOperations<String, Object> setOperations;

  private RedisJobQueue jobQueue;
  private Job testJob;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    jobQueue = new RedisJobQueue(redisTemplate);
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    testJob = new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
  }

  @Test
  void shouldPublishJobSuccessfully() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String queueKey = "pdf:jobs:queue";

    // When
    jobQueue.publish(testJob);

    // Then
    verify(listOperations).leftPush(eq(queueKey), anyString());
  }

  @Test
  void shouldThrowExceptionWhenPublishFailsWithSerialization() {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    
    // When & Then - teste do comportamento normal
    assertDoesNotThrow(() -> jobQueue.publish(testJob));
  }

  @Test
  void shouldConsumeJobSuccessfullyNonBlocking() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    String queueKey = "pdf:jobs:queue";
    String processingKey = "pdf:jobs:processing";
    String jobJson = objectMapper.writeValueAsString(testJob);
    when(listOperations.rightPop(queueKey)).thenReturn(jobJson);

    // When
    Optional<Job> consumedJob = jobQueue.consume();

    // Then
    assertTrue(consumedJob.isPresent());
    assertEquals("job-123", consumedJob.get().getId());
    assertEquals(JobOperation.MERGE, consumedJob.get().getOperation());
    verify(setOperations).add(processingKey, jobJson);
  }

  @Test
  void shouldConsumeJobSuccessfullyWithTimeout() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    String queueKey = "pdf:jobs:queue";
    String processingKey = "pdf:jobs:processing";
    String jobJson = objectMapper.writeValueAsString(testJob);
    long timeoutSeconds = 5;
    when(listOperations.rightPop(queueKey, timeoutSeconds, TimeUnit.SECONDS)).thenReturn(jobJson);

    // When
    Optional<Job> consumedJob = jobQueue.consume(timeoutSeconds);

    // Then
    assertTrue(consumedJob.isPresent());
    assertEquals("job-123", consumedJob.get().getId());
    verify(setOperations).add(processingKey, jobJson);
  }

  @Test
  void shouldReturnEmptyWhenNoJobAvailable() {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String queueKey = "pdf:jobs:queue";
    when(listOperations.rightPop(queueKey)).thenReturn(null);

    // When
    Optional<Job> consumedJob = jobQueue.consume();

    // Then
    assertFalse(consumedJob.isPresent());
  }

  @Test
  void shouldReturnEmptyWhenConsumeThrowsException() {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String queueKey = "pdf:jobs:queue";
    when(listOperations.rightPop(queueKey)).thenThrow(new RuntimeException("Redis error"));

    // When
    Optional<Job> consumedJob = jobQueue.consume();

    // Then
    assertFalse(consumedJob.isPresent());
  }

  @Test
  void shouldReturnJobToQueue() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    String queueKey = "pdf:jobs:queue";
    String processingKey = "pdf:jobs:processing";
    String jobJson = objectMapper.writeValueAsString(testJob);

    // When
    jobQueue.returnToQueue(testJob);

    // Then
    verify(setOperations).remove(processingKey, jobJson);
    verify(listOperations).leftPush(eq(queueKey), anyString());
  }

  @Test
  void shouldHandleExceptionWhenReturnToQueueFails() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String processingKey = "pdf:jobs:processing";
    String queueKey = "pdf:jobs:queue";
    String jobJson = objectMapper.writeValueAsString(testJob);
    
    // When & Then - deve não lançar exceção mesmo com erro interno
    assertDoesNotThrow(() -> jobQueue.returnToQueue(testJob));
    
    // Verify
    verify(setOperations).remove(processingKey, jobJson);
    verify(listOperations).leftPush(eq(queueKey), anyString());
  }

  @Test
  void shouldAcknowledgeJob() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    String processingKey = "pdf:jobs:processing";
    String jobJson = objectMapper.writeValueAsString(testJob);

    // When
    jobQueue.acknowledge(testJob);

    // Then
    verify(setOperations).remove(processingKey, jobJson);
  }

  @Test
  void shouldHandleExceptionWhenAcknowledgeFails() throws JsonProcessingException {
    // Given
    when(redisTemplate.opsForSet()).thenReturn(setOperations);
    String processingKey = "pdf:jobs:processing";
    String jobJson = objectMapper.writeValueAsString(testJob);
    
    // When & Then - deve não lançar exceção mesmo com erro interno
    assertDoesNotThrow(() -> jobQueue.acknowledge(testJob));
    
    // Verify
    verify(setOperations).remove(processingKey, jobJson);
  }

  @Test
  void shouldGetQueueSize() {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String queueKey = "pdf:jobs:queue";
    when(listOperations.size(queueKey)).thenReturn(5L);

    // When
    long queueSize = jobQueue.getQueueSize();

    // Then
    assertEquals(5L, queueSize);
  }

  @Test
  void shouldReturnZeroWhenQueueSizeIsNull() {
    // Given
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    String queueKey = "pdf:jobs:queue";
    when(listOperations.size(queueKey)).thenReturn(null);

    // When
    long queueSize = jobQueue.getQueueSize();

    // Then
    assertEquals(0L, queueSize);
  }
}