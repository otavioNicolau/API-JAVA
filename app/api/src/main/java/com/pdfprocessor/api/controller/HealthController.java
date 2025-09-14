package com.pdfprocessor.api.controller;

import com.pdfprocessor.domain.port.JobQueue;
import com.pdfprocessor.domain.port.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller para verificações de saúde da aplicação. */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Verificações de saúde da aplicação")
public class HealthController {

  private final JobQueue jobQueue;
  private final StorageService storageService;

  public HealthController(JobQueue jobQueue, StorageService storageService) {
    this.jobQueue = jobQueue;
    this.storageService = storageService;
  }

  @GetMapping
  @Operation(
      summary = "Verificação básica de saúde",
      description = "Retorna o status básico da aplicação")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status da aplicação",
            content =
                @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value =
                                "{\"status\":\"UP\",\"timestamp\":\"2024-01-15T10:30:00\",\"service\":\"pdf-processor-api\",\"version\":\"1.0.0\"}")))
      })
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now());
    health.put("service", "pdf-processor-api");
    health.put("version", "1.0.0");

    return ResponseEntity.ok(health);
  }

  @GetMapping("/detailed")
  @Operation(
      summary = "Verificação detalhada de saúde",
      description =
          "Retorna o status detalhado da aplicação incluindo componentes (fila Redis e storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status detalhado da aplicação",
            content =
                @Content(
                    mediaType = "application/json",
                    examples = {
                      @ExampleObject(
                          name = "all_up",
                          value =
                              "{\"status\":\"UP\",\"timestamp\":\"2024-01-15T10:30:00\",\"service\":\"pdf-processor-api\",\"version\":\"1.0.0\",\"components\":{\"queue\":{\"status\":\"UP\",\"size\":5},\"storage\":{\"status\":\"UP\",\"basePath\":\"./storage\"}}}",
                          description = "Todos os componentes funcionando"),
                      @ExampleObject(
                          name = "degraded",
                          value =
                              "{\"status\":\"DEGRADED\",\"timestamp\":\"2024-01-15T10:30:00\",\"service\":\"pdf-processor-api\",\"version\":\"1.0.0\",\"components\":{\"queue\":{\"status\":\"DOWN\",\"error\":\"Connection refused\"},\"storage\":{\"status\":\"UP\",\"basePath\":\"./storage\"}}}",
                          description = "Alguns componentes com problemas")
                    }))
      })
  public ResponseEntity<Map<String, Object>> detailedHealth() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now());
    health.put("service", "pdf-processor-api");
    health.put("version", "1.0.0");

    // Verificar componentes
    Map<String, Object> components = new HashMap<>();

    // Verificar fila
    try {
      long queueSize = jobQueue.getQueueSize();
      components.put("queue", Map.of("status", "UP", "size", queueSize));
    } catch (Exception e) {
      components.put("queue", Map.of("status", "DOWN", "error", e.getMessage()));
    }

    // Verificar storage
    try {
      boolean storageOk = storageService.exists("./storage");
      components.put(
          "storage", Map.of("status", storageOk ? "UP" : "DOWN", "basePath", "./storage"));
    } catch (Exception e) {
      components.put("storage", Map.of("status", "DOWN", "error", e.getMessage()));
    }

    health.put("components", components);

    // Determinar status geral
    boolean allUp =
        components.values().stream()
            .allMatch(
                component -> {
                  if (component instanceof Map) {
                    return "UP".equals(((Map<?, ?>) component).get("status"));
                  }
                  return false;
                });

    health.put("status", allUp ? "UP" : "DEGRADED");

    return ResponseEntity.ok(health);
  }
}
