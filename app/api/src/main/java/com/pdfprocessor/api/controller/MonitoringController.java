package com.pdfprocessor.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller para endpoints de monitoramento e health check. */
@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name = "Monitoring", description = "Endpoints de monitoramento e health check")
public class MonitoringController {

  @Autowired(required = false)
  private RedisTemplate<String, Object> redisTemplate;

  @GetMapping("/health")
  @Operation(summary = "Health check detalhado do sistema")
  public ResponseEntity<Map<String, Object>> healthCheck() {
    Map<String, Object> health = new HashMap<>();
    health.put("timestamp", LocalDateTime.now());
    health.put("status", "UP");
    health.put("application", "PDF Processor API");
    health.put("version", "1.0.0");

    // Verificar componentes
    Map<String, Object> components = new HashMap<>();

    // Redis health
    components.put("redis", checkRedisHealth());

    // Storage health
    components.put("storage", checkStorageHealth());

    // Memory info
    components.put("memory", getMemoryInfo());

    health.put("components", components);

    return ResponseEntity.ok(health);
  }

  @GetMapping("/metrics")
  @Operation(summary = "Métricas básicas do sistema")
  public ResponseEntity<Map<String, Object>> metrics() {
    Map<String, Object> metrics = new HashMap<>();

    Runtime runtime = Runtime.getRuntime();
    metrics.put("memory", getMemoryInfo());
    metrics.put("processors", runtime.availableProcessors());
    metrics.put("timestamp", LocalDateTime.now());

    return ResponseEntity.ok(metrics);
  }

  private Map<String, Object> checkRedisHealth() {
    Map<String, Object> redisHealth = new HashMap<>();
    try {
      if (redisTemplate != null) {
        redisTemplate.opsForValue().set("health-check", "test");
        String result = (String) redisTemplate.opsForValue().get("health-check");
        redisHealth.put("status", "test".equals(result) ? "UP" : "DOWN");
        redisTemplate.delete("health-check");
      } else {
        redisHealth.put("status", "DOWN");
        redisHealth.put("error", "Redis template not available");
      }
    } catch (Exception e) {
      redisHealth.put("status", "DOWN");
      redisHealth.put("error", e.getMessage());
    }
    return redisHealth;
  }

  private Map<String, Object> checkStorageHealth() {
    Map<String, Object> storageHealth = new HashMap<>();
    try {
      File storageDir = new File("./storage");
      if (!storageDir.exists()) {
        storageDir.mkdirs();
      }

      storageHealth.put("status", storageDir.canRead() && storageDir.canWrite() ? "UP" : "DOWN");
      storageHealth.put("path", storageDir.getAbsolutePath());
      storageHealth.put("freeSpace", storageDir.getFreeSpace());
      storageHealth.put("totalSpace", storageDir.getTotalSpace());
    } catch (Exception e) {
      storageHealth.put("status", "DOWN");
      storageHealth.put("error", e.getMessage());
    }
    return storageHealth;
  }

  private Map<String, Object> getMemoryInfo() {
    Runtime runtime = Runtime.getRuntime();
    Map<String, Object> memory = new HashMap<>();

    long maxMemory = runtime.maxMemory();
    long totalMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long usedMemory = totalMemory - freeMemory;

    memory.put("max", maxMemory);
    memory.put("total", totalMemory);
    memory.put("used", usedMemory);
    memory.put("free", freeMemory);
    memory.put("usagePercent", Math.round((double) usedMemory / totalMemory * 100));

    return memory;
  }
}
