package com.pdfprocessor.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementação do repositório de jobs usando Redis Hash. Cada job é armazenado como um hash no
 * Redis.
 */
@Component
public class RedisJobRepository implements JobRepository {

  private static final String JOBS_KEY_PREFIX = "pdf:job:";
  private static final String JOBS_INDEX_KEY = "pdf:jobs:index";

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Autowired
  public RedisJobRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @Override
  public Job save(Job job) {
    try {
      String jobKey = JOBS_KEY_PREFIX + job.getId();
      String jobJson = objectMapper.writeValueAsString(job);

      // Save job as hash
      redisTemplate.opsForValue().set(jobKey, jobJson);

      // Add to index for listing
      redisTemplate.opsForSet().add(JOBS_INDEX_KEY, job.getId());

      System.out.println("Saved job to Redis: " + job.getId());
      return job;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize job: " + job.getId(), e);
    }
  }

  @Override
  public Optional<Job> findById(String id) {
    try {
      String jobKey = JOBS_KEY_PREFIX + id;
      String jobJson = (String) redisTemplate.opsForValue().get(jobKey);

      if (jobJson != null) {
        Job job = objectMapper.readValue(jobJson, Job.class);
        System.out.println("Found job in Redis: " + id);
        return Optional.of(job);
      }

      System.out.println("Job not found in Redis: " + id);
      return Optional.empty();
    } catch (Exception e) {
      System.err.println("Failed to find job in Redis: " + id + ", " + e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public List<Job> findAll(int page, int size) {
    try {
      Set<Object> jobIds = redisTemplate.opsForSet().members(JOBS_INDEX_KEY);

      if (jobIds == null || jobIds.isEmpty()) {
        return List.of();
      }

      return jobIds.stream()
          .map(Object::toString)
          .skip((long) page * size)
          .limit(size)
          .map(this::findById)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    } catch (Exception e) {
      System.err.println("Failed to find all jobs in Redis: " + e.getMessage());
      return List.of();
    }
  }

  @Override
  public void deleteById(String id) {
    try {
      String jobKey = JOBS_KEY_PREFIX + id;

      // Remove job data
      redisTemplate.delete(jobKey);

      // Remove from index
      redisTemplate.opsForSet().remove(JOBS_INDEX_KEY, id);

      System.out.println("Deleted job from Redis: " + id);
    } catch (Exception e) {
      System.err.println("Failed to delete job from Redis: " + id + ", " + e.getMessage());
    }
  }

  @Override
  public boolean existsById(String id) {
    try {
      String jobKey = JOBS_KEY_PREFIX + id;
      Boolean exists = redisTemplate.hasKey(jobKey);
      boolean result = exists != null && exists;
      System.out.println("Checking if job exists in Redis: " + id + " -> " + result);
      return result;
    } catch (Exception e) {
      System.err.println("Failed to check job existence in Redis: " + id + ", " + e.getMessage());
      return false;
    }
  }
}
