package com.pdfprocessor.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobQueue;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementação da fila de jobs usando Redis Lists. Usa LPUSH para adicionar jobs e BRPOP para
 * consumir com timeout.
 */
@Component
public class RedisJobQueue implements JobQueue {

  private static final String QUEUE_KEY = "pdf:jobs:queue";
  private static final String PROCESSING_KEY = "pdf:jobs:processing";

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Autowired
  public RedisJobQueue(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public void publish(Job job) {
    try {
      String jobJson = objectMapper.writeValueAsString(job);
      redisTemplate.opsForList().leftPush(QUEUE_KEY, jobJson);
      System.out.println("Published job to Redis queue: " + job.getId());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize job: " + job.getId(), e);
    }
  }

  @Override
  public Optional<Job> consume() {
    return consume(0); // Non-blocking
  }

  @Override
  public Optional<Job> consume(long timeoutSeconds) {
    try {
      Object result;
      if (timeoutSeconds > 0) {
        // Blocking pop with timeout
        result = redisTemplate.opsForList().rightPop(QUEUE_KEY, timeoutSeconds, TimeUnit.SECONDS);
      } else {
        // Non-blocking pop
        result = redisTemplate.opsForList().rightPop(QUEUE_KEY);
      }

      if (result != null) {
        String jobJson = (String) result;
        Job job = objectMapper.readValue(jobJson, Job.class);

        // Move to processing set for tracking
        redisTemplate.opsForSet().add(PROCESSING_KEY, jobJson);

        System.out.println("Consumed job from Redis queue: " + job.getId());
        return Optional.of(job);
      }

      return Optional.empty();
    } catch (Exception e) {
      System.err.println("Failed to consume job from queue: " + e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void returnToQueue(Job job) {
    try {
      String jobJson = objectMapper.writeValueAsString(job);
      // Remove from processing and return to queue
      redisTemplate.opsForSet().remove(PROCESSING_KEY, jobJson);
      redisTemplate.opsForList().leftPush(QUEUE_KEY, jobJson);
      System.out.println("Returned job to Redis queue: " + job.getId());
    } catch (JsonProcessingException e) {
      System.err.println("Failed to return job to queue: " + job.getId() + ", " + e.getMessage());
    }
  }

  @Override
  public void acknowledge(Job job) {
    try {
      String jobJson = objectMapper.writeValueAsString(job);
      // Remove from processing set
      redisTemplate.opsForSet().remove(PROCESSING_KEY, jobJson);
      System.out.println("Acknowledged job: " + job.getId());
    } catch (JsonProcessingException e) {
      System.err.println("Failed to acknowledge job: " + job.getId() + ", " + e.getMessage());
    }
  }

  @Override
  public long getQueueSize() {
    Long size = redisTemplate.opsForList().size(QUEUE_KEY);
    return size != null ? size : 0;
  }
}
