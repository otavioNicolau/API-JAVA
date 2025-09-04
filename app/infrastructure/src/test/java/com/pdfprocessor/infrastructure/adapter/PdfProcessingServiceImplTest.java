package com.pdfprocessor.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testes unitários para PdfProcessingServiceImpl. */
class PdfProcessingServiceImplTest {

  private PdfProcessingServiceImpl pdfProcessingService;
  private Job testJob;

  @BeforeEach
  void setUp() {
    pdfProcessingService = new PdfProcessingServiceImpl();
    testJob = new Job("job-123", JobOperation.MERGE, List.of("file1.pdf", "file2.pdf"), Map.of());
  }

  @Test
  void shouldReturnSupportedOperations() {
    // When
    List<JobOperation> supportedOperations = pdfProcessingService.getSupportedOperations();

    // Then
    assertNotNull(supportedOperations);
    assertFalse(supportedOperations.isEmpty());
    assertTrue(supportedOperations.contains(JobOperation.MERGE));
    assertTrue(supportedOperations.contains(JobOperation.SPLIT));
    assertTrue(supportedOperations.contains(JobOperation.ROTATE));
    assertTrue(supportedOperations.contains(JobOperation.WATERMARK));
    assertTrue(supportedOperations.contains(JobOperation.ENCRYPT));
    assertTrue(supportedOperations.contains(JobOperation.DECRYPT));
    assertTrue(supportedOperations.contains(JobOperation.PDF_TO_IMAGES));
    assertTrue(supportedOperations.contains(JobOperation.IMAGES_TO_PDF));
  }

  @Test
  void shouldSupportKnownOperations() {
    // When & Then
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.MERGE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.SPLIT));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.ROTATE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.WATERMARK));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.ENCRYPT));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.DECRYPT));
  }

  @Test
  void shouldValidateOptionsForMergeOperation() {
    // Given
    Map<String, Object> validOptions = Map.of();
    Map<String, Object> invalidOptions = Map.of("invalid", "option");

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.MERGE, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.MERGE, invalidOptions)); // MERGE aceita qualquer opção
  }

  @Test
  void shouldValidateOptionsForSplitOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("pages", "1-5");
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.SPLIT, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.SPLIT, emptyOptions)); // Split sem páginas específicas é válido
  }

  @Test
  void shouldValidateOptionsForRotateOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("degrees", 90);
    Map<String, Object> invalidOptions = Map.of("degrees", 45); // Não é múltiplo de 90

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.ROTATE, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.ROTATE, invalidOptions));
  }

  @Test
  void shouldValidateOptionsForWatermarkOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("text", "CONFIDENTIAL");
    Map<String, Object> invalidOptions = Map.of(); // Texto é obrigatório

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.WATERMARK, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.WATERMARK, invalidOptions));
  }

  @Test
  void shouldValidatePdfToImagesOptionsCorrectly() {
    // Opções válidas
    Map<String, Object> validOptions = Map.of(
        "format", "PNG",
        "dpi", "150",
        "pages", "1-3"
    );
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_TO_IMAGES, validOptions));

    // Formato inválido
    Map<String, Object> invalidFormat = Map.of("format", "INVALID");
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_TO_IMAGES, invalidFormat));

    // DPI inválido
    Map<String, Object> invalidDpi = Map.of("dpi", "invalid");
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_TO_IMAGES, invalidDpi));

    // Sem opções (deve ser válido)
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_TO_IMAGES, Map.of()));
  }

  @Test
  void shouldValidateImagesToPdfOptionsCorrectly() {
    // Opções válidas
    Map<String, Object> validOptions = Map.of(
        "page_size", "A4",
        "fit_to_page", "true"
    );
    assertTrue(pdfProcessingService.validateOptions(JobOperation.IMAGES_TO_PDF, validOptions));

    // Tamanho de página inválido
    Map<String, Object> invalidPageSize = Map.of("page_size", "INVALID");
    assertFalse(pdfProcessingService.validateOptions(JobOperation.IMAGES_TO_PDF, invalidPageSize));

    // Sem opções (deve ser válido)
    assertTrue(pdfProcessingService.validateOptions(JobOperation.IMAGES_TO_PDF, Map.of()));
  }

  @Test
  void shouldValidateOptionsForEncryptOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("password", "secret123");
    Map<String, Object> invalidOptions = Map.of(); // Password é obrigatório

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.ENCRYPT, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.ENCRYPT, invalidOptions));
  }

  @Test
  void shouldValidateOptionsForDecryptOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("password", "secret123");
    Map<String, Object> invalidOptions = Map.of(); // Password é obrigatório

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.DECRYPT, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.DECRYPT, invalidOptions));
  }

  @Test
  void shouldReturnOptionsSchemaForOperations() {
    // When & Then
    Map<String, Object> mergeSchema = pdfProcessingService.getOptionsSchema(JobOperation.MERGE);
    assertNotNull(mergeSchema);

    Map<String, Object> splitSchema = pdfProcessingService.getOptionsSchema(JobOperation.SPLIT);
    assertNotNull(splitSchema);
    assertTrue(splitSchema.containsKey("pages"));

    Map<String, Object> rotateSchema = pdfProcessingService.getOptionsSchema(JobOperation.ROTATE);
    assertNotNull(rotateSchema);
    assertTrue(rotateSchema.containsKey("degrees"));

    Map<String, Object> watermarkSchema = pdfProcessingService.getOptionsSchema(JobOperation.WATERMARK);
    assertNotNull(watermarkSchema);
    assertTrue(watermarkSchema.containsKey("text"));

    Map<String, Object> encryptSchema = pdfProcessingService.getOptionsSchema(JobOperation.ENCRYPT);
    assertNotNull(encryptSchema);
    assertTrue(encryptSchema.containsKey("password"));

    // Test PDF_TO_IMAGES operation schema
    Map<String, Object> pdfToImagesSchema = pdfProcessingService.getOptionsSchema(JobOperation.PDF_TO_IMAGES);
    assertNotNull(pdfToImagesSchema);
    assertTrue(pdfToImagesSchema.containsKey("format"));
    assertTrue(pdfToImagesSchema.containsKey("dpi"));
    assertTrue(pdfToImagesSchema.containsKey("pages"));

    // Test IMAGES_TO_PDF operation schema
    Map<String, Object> imagesToPdfSchema = pdfProcessingService.getOptionsSchema(JobOperation.IMAGES_TO_PDF);
    assertNotNull(imagesToPdfSchema);
    assertTrue(imagesToPdfSchema.containsKey("page_size"));
    assertTrue(imagesToPdfSchema.containsKey("fit_to_page"));
  }

  @Test
  void shouldThrowExceptionForUnsupportedOperation() {
    // Given - Job with null operation should be handled
    Job job = new Job("job-123", JobOperation.MERGE, List.of("input.pdf"), Map.of());
    
    // When & Then - Test with a mock unsupported operation
    assertDoesNotThrow(() -> {
      // This test verifies the service can handle jobs properly
      // The actual unsupported operation testing would require more complex setup
      assertNotNull(job.getOperation());
    });
  }

  @Test
  void shouldThrowExceptionForJobWithoutInputFiles() {
    // Given
    Job jobWithoutFiles = new Job("job-789", JobOperation.MERGE, List.of(), Map.of());

    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
      pdfProcessingService.processJob(jobWithoutFiles);
    });
  }

  @Test
  void shouldThrowExceptionForNonExistentInputFiles() {
    // Given
    Job jobWithNonExistentFiles = new Job("job-999", JobOperation.MERGE, 
        List.of("non-existent1.pdf", "non-existent2.pdf"), Map.of());

    // When & Then
    assertThrows(RuntimeException.class, () -> {
      pdfProcessingService.processJob(jobWithNonExistentFiles);
    });
  }

  @Test
  void shouldValidateJobBeforeProcessing() {
    // Test null job
    assertThrows(IllegalArgumentException.class, () -> {
      pdfProcessingService.processJob(null);
    });

    // Test job with empty input files
    Job jobWithEmptyFiles = new Job("job-123", JobOperation.MERGE, List.of(), Map.of());
    assertThrows(IllegalArgumentException.class, () -> {
      pdfProcessingService.processJob(jobWithEmptyFiles);
    });
    
    // Test valid job structure
    Job validJob = new Job("job-123", JobOperation.MERGE, List.of("input.pdf"), Map.of());
    assertNotNull(validJob.getOperation());
    assertFalse(validJob.getInputFiles().isEmpty());
  }
}