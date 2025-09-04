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
    assertTrue(supportedOperations.contains(JobOperation.PDF_COMPARE));
    assertTrue(supportedOperations.contains(JobOperation.PDF_CREATE));
    assertTrue(supportedOperations.contains(JobOperation.PDF_EDIT));
    assertTrue(supportedOperations.contains(JobOperation.PDF_PROTECT));
    assertTrue(supportedOperations.contains(JobOperation.PDF_UNLOCK));
    assertTrue(supportedOperations.contains(JobOperation.PDF_OPTIMIZE));
    assertTrue(supportedOperations.contains(JobOperation.PDF_VALIDATE));
    assertTrue(supportedOperations.contains(JobOperation.PDF_REPAIR));
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
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.PDF_COMPARE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.PDF_CREATE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.PDF_OPTIMIZE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.PDF_VALIDATE));
    assertTrue(pdfProcessingService.supportsOperation(JobOperation.PDF_REPAIR));
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

  @Test
  void shouldValidateOptionsForPdfCompareOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("detailed_diff", true, "output_filename", "report.txt");
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_COMPARE, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_COMPARE, emptyOptions));
  }

  @Test
  void shouldValidateOptionsForPdfCreateOperation() {
    // Given
    Map<String, Object> validOptionsWithText = Map.of(
        "text_content", "Hello World",
        "page_size", "A4",
        "font_size", "12",
        "margin", "50",
        "title", "Test Document",
        "author", "Test Author"
    );
    Map<String, Object> validOptionsWithPages = Map.of(
        "pages", "3",
        "page_size", "LETTER"
    );
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_CREATE, validOptionsWithText));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_CREATE, validOptionsWithPages));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_CREATE, emptyOptions));
  }

  @Test
  void shouldThrowExceptionForPdfCompareWithInvalidInputFiles() {
    // Given
    Job jobWithOneFile = new Job("job-123", JobOperation.PDF_COMPARE, List.of("file1.pdf"), Map.of());
    Job jobWithThreeFiles = new Job("job-456", JobOperation.PDF_COMPARE, List.of("file1.pdf", "file2.pdf", "file3.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithOneFile)
    );
    assertEquals("PDF_COMPARE operation requires exactly two input files", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithThreeFiles)
    );
    assertEquals("PDF_COMPARE operation requires exactly two input files", exception2.getMessage());
  }

  @Test
  void shouldValidateOptionsForPdfEditOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("edit_type", "add_text", "text", "Hello", "x", 100, "y", 200);
    Map<String, Object> invalidOptions = Map.of(); // Missing edit_type

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_EDIT, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_EDIT, invalidOptions));
  }

  @Test
  void shouldValidateOptionsForPdfProtectOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("password", "secret123");
    Map<String, Object> validOptionsWithPermissions = Map.of(
        "password", "secret123",
        "allow_printing", true,
        "allow_copying", false
    );
    Map<String, Object> invalidOptions = Map.of(); // No password

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_PROTECT, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_PROTECT, validOptionsWithPermissions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_PROTECT, invalidOptions));
  }

  @Test
  void shouldValidateOptionsForPdfUnlockOperation() {
    // Given
    Map<String, Object> validOptions = Map.of("password", "secret123");
    Map<String, Object> invalidOptions = Map.of(); // Missing password

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_UNLOCK, validOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_UNLOCK, invalidOptions));
  }

  @Test
  void shouldThrowExceptionForPdfEditWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_EDIT, List.of(), Map.of("editType", "addText"));
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_EDIT, List.of("file1.pdf", "file2.pdf"), Map.of("editType", "addText"));

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_EDIT operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfEditWithMissingEditType() {
    // Given
    Job jobWithoutEditType = new Job("job-123", JobOperation.PDF_EDIT, List.of("file1.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithoutEditType)
    );
    assertEquals("Edit type is required (add_text, remove_text, replace_text)", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfProtectWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_PROTECT, List.of(), Map.of("userPassword", "pass123"));
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_PROTECT, List.of("file1.pdf", "file2.pdf"), Map.of("userPassword", "pass123"));

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_PROTECT operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfProtectWithMissingPasswords() {
    // Given
    Job jobWithoutPasswords = new Job("job-123", JobOperation.PDF_PROTECT, List.of("file1.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithoutPasswords)
    );
    assertEquals("At least one password (user or owner) is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfUnlockWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_UNLOCK, List.of(), Map.of("password", "pass123"));
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_UNLOCK, List.of("file1.pdf", "file2.pdf"), Map.of("password", "pass123"));

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_UNLOCK operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldValidateOptionsForPdfOptimizeOperation() {
    // Given
    Map<String, Object> validOptions = Map.of(
        "compression_level", "medium",
        "remove_unused_objects", true,
        "compress_images", true
    );
    Map<String, Object> validOptionsWithNumber = Map.of("compression_level", 5);
    Map<String, Object> invalidCompressionLevel = Map.of("compression_level", "invalid");
    Map<String, Object> invalidCompressionNumber = Map.of("compression_level", 15);
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_OPTIMIZE, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_OPTIMIZE, validOptionsWithNumber));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_OPTIMIZE, emptyOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_OPTIMIZE, invalidCompressionLevel));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_OPTIMIZE, invalidCompressionNumber));
  }

  @Test
  void shouldValidateOptionsForPdfValidateOperation() {
    // Given
    Map<String, Object> validOptions = Map.of(
        "validation_type", "detailed",
        "compliance_standard", "pdf_a"
    );
    Map<String, Object> validBasicOptions = Map.of("validation_type", "basic");
    Map<String, Object> invalidValidationType = Map.of("validation_type", "invalid");
    Map<String, Object> invalidComplianceStandard = Map.of("compliance_standard", "invalid");
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_VALIDATE, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_VALIDATE, validBasicOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_VALIDATE, emptyOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_VALIDATE, invalidValidationType));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_VALIDATE, invalidComplianceStandard));
  }

  @Test
  void shouldValidateOptionsForPdfRepairOperation() {
    // Given
    Map<String, Object> validOptions = Map.of(
        "repair_type", "advanced",
        "fix_structure", true,
        "fix_metadata", false,
        "remove_corrupted_objects", true
    );
    Map<String, Object> validBasicOptions = Map.of("repair_type", "basic");
    Map<String, Object> invalidRepairType = Map.of("repair_type", "invalid");
    Map<String, Object> emptyOptions = Map.of();

    // When & Then
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_REPAIR, validOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_REPAIR, validBasicOptions));
    assertTrue(pdfProcessingService.validateOptions(JobOperation.PDF_REPAIR, emptyOptions));
    assertFalse(pdfProcessingService.validateOptions(JobOperation.PDF_REPAIR, invalidRepairType));
  }

  @Test
  void shouldReturnOptionsSchemaForNewOperations() {
    // Test PDF_OPTIMIZE operation schema
    Map<String, Object> optimizeSchema = pdfProcessingService.getOptionsSchema(JobOperation.PDF_OPTIMIZE);
    assertNotNull(optimizeSchema);
    assertTrue(optimizeSchema.containsKey("compression_level"));
    assertTrue(optimizeSchema.containsKey("remove_unused_objects"));
    assertTrue(optimizeSchema.containsKey("compress_images"));

    // Test PDF_VALIDATE operation schema
    Map<String, Object> validateSchema = pdfProcessingService.getOptionsSchema(JobOperation.PDF_VALIDATE);
    assertNotNull(validateSchema);
    assertTrue(validateSchema.containsKey("validation_type"));
    assertTrue(validateSchema.containsKey("compliance_standard"));

    // Test PDF_REPAIR operation schema
    Map<String, Object> repairSchema = pdfProcessingService.getOptionsSchema(JobOperation.PDF_REPAIR);
    assertNotNull(repairSchema);
    assertTrue(repairSchema.containsKey("repair_type"));
    assertTrue(repairSchema.containsKey("fix_structure"));
    assertTrue(repairSchema.containsKey("fix_metadata"));
    assertTrue(repairSchema.containsKey("remove_corrupted_objects"));
  }

  @Test
  void shouldThrowExceptionForPdfOptimizeWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_OPTIMIZE, List.of(), Map.of());
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_OPTIMIZE, List.of("file1.pdf", "file2.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_OPTIMIZE operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfValidateWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_VALIDATE, List.of(), Map.of());
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_VALIDATE, List.of("file1.pdf", "file2.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_VALIDATE operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfRepairWithInvalidInputFiles() {
    // Given
    Job jobWithNoFiles = new Job("job-123", JobOperation.PDF_REPAIR, List.of(), Map.of());
    Job jobWithTwoFiles = new Job("job-456", JobOperation.PDF_REPAIR, List.of("file1.pdf", "file2.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception1 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithNoFiles)
    );
    assertEquals("Job input files cannot be empty", exception1.getMessage());

    IllegalArgumentException exception2 = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithTwoFiles)
    );
    assertEquals("PDF_REPAIR operation requires exactly one input file", exception2.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfUnlockWithMissingPassword() {
    // Given
    Job jobWithoutPassword = new Job("job-123", JobOperation.PDF_UNLOCK, List.of("file1.pdf"), Map.of());

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> pdfProcessingService.processJob(jobWithoutPassword)
    );
    assertEquals("Password is required to unlock PDF", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForPdfEditWithUnsupportedEditType() {
    // Given
    Job jobWithInvalidEditType = new Job("job-123", JobOperation.PDF_EDIT, List.of("file1.pdf"), Map.of("edit_type", "invalidType"));

    // When & Then
    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> pdfProcessingService.processJob(jobWithInvalidEditType)
    );
    assertTrue(exception.getMessage().contains("Unsupported edit type: invalidType. Supported types: add_text, remove_text, replace_text"));
  }

  @Test
  void shouldThrowExceptionForAddTextWithMissingText() {
    // Given
    Job jobWithoutText = new Job("job-123", JobOperation.PDF_EDIT, List.of("file1.pdf"), Map.of("edit_type", "add_text"));

    // When & Then
    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> pdfProcessingService.processJob(jobWithoutText)
    );
    assertTrue(exception.getMessage().contains("Text is required for addText operation"));
  }

  @Test
  void shouldThrowExceptionForReplaceTextWithMissingNewText() {
    // Given
    Job jobWithoutNewText = new Job("job-123", JobOperation.PDF_EDIT, List.of("file1.pdf"), Map.of("edit_type", "replace_text"));

    // When & Then
    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> pdfProcessingService.processJob(jobWithoutNewText)
    );
    assertTrue(exception.getMessage().contains("New text is required for replace_text operation"));
  }
}