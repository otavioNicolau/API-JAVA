package com.pdfprocessor.api.controller;

import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.PdfProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller para informações sobre operações suportadas. */
@RestController
@RequestMapping("/api/v1/operations")
@Tag(name = "Operations", description = "Informações sobre operações de processamento suportadas")
public class OperationsController {

  private final PdfProcessingService pdfProcessingService;

  public OperationsController(PdfProcessingService pdfProcessingService) {
    this.pdfProcessingService = pdfProcessingService;
  }

  @GetMapping
  @Operation(summary = "Listar todas as operações suportadas")
  public ResponseEntity<List<OperationInfo>> getSupportedOperations() {
    List<JobOperation> operations = pdfProcessingService.getSupportedOperations();

    List<OperationInfo> operationInfos =
        operations.stream()
            .map(
                op ->
                    new OperationInfo(
                        op.name(), op.getDescription(), pdfProcessingService.getOptionsSchema(op)))
            .toList();

    return ResponseEntity.ok(operationInfos);
  }

  @GetMapping("/{operation}")
  @Operation(summary = "Obter informações sobre uma operação específica")
  public ResponseEntity<OperationInfo> getOperationInfo(@PathVariable String operation) {
    try {
      JobOperation jobOperation = JobOperation.valueOf(operation.toUpperCase());

      if (!pdfProcessingService.supportsOperation(jobOperation)) {
        return ResponseEntity.notFound().build();
      }

      OperationInfo info =
          new OperationInfo(
              jobOperation.name(),
              jobOperation.getDescription(),
              pdfProcessingService.getOptionsSchema(jobOperation));

      return ResponseEntity.ok(info);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{operation}/schema")
  @Operation(summary = "Obter esquema de opções para uma operação")
  public ResponseEntity<Map<String, Object>> getOperationSchema(@PathVariable String operation) {
    try {
      JobOperation jobOperation = JobOperation.valueOf(operation.toUpperCase());

      if (!pdfProcessingService.supportsOperation(jobOperation)) {
        return ResponseEntity.notFound().build();
      }

      Map<String, Object> schema = pdfProcessingService.getOptionsSchema(jobOperation);
      return ResponseEntity.ok(schema);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /** DTO para informações de operação. */
  public static class OperationInfo {
    private final String name;
    private final String description;
    private final Map<String, Object> optionsSchema;

    public OperationInfo(String name, String description, Map<String, Object> optionsSchema) {
      this.name = name;
      this.description = description;
      this.optionsSchema = optionsSchema;
    }

    public String getName() {
      return name;
    }

    public String getDescription() {
      return description;
    }

    public Map<String, Object> getOptionsSchema() {
      return optionsSchema;
    }
  }
}
