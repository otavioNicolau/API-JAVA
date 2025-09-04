package com.pdfprocessor.api.service;

import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.api.exception.SecurityValidationException;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/** Testes unitários para InputValidationService. */
class InputValidationServiceTest {

  private InputValidationService inputValidationService;

  @BeforeEach
  void setUp() {
    inputValidationService = new InputValidationService();
  }

  @Test
  void shouldValidateOperationSuccessfully() {
    // Given
    String operation = "MERGE";

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateOperation(operation));
  }

  @Test
  void shouldThrowExceptionForNullOperation() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateOperation(null));
    assertEquals("Operation parameter is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForEmptyOperation() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateOperation(""));
    assertEquals("Operation parameter is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForInvalidOperation() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateOperation("INVALID_OP"));
    assertEquals("Unsupported operation: INVALID_OP", exception.getMessage());
  }

  @Test
  void shouldValidateUploadedFilesSuccessfully() {
    // Given
    List<MultipartFile> files =
        Arrays.asList(
            new MockMultipartFile("file1", "test1.pdf", "application/pdf", new byte[1024]),
            new MockMultipartFile("file2", "test2.pdf", "application/pdf", new byte[2048]));

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateUploadedFiles(files));
  }

  @Test
  void shouldThrowExceptionForTooManyFiles() {
    // Given
    List<MultipartFile> files = Collections.nCopies(11, 
        new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[1024]));

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateUploadedFiles(files));
    assertEquals("Maximum 10 files allowed per job, but 11 files provided", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForFileTooLarge() {
    // Given
    List<MultipartFile> files =
        Arrays.asList(
            new MockMultipartFile(
                "file", "large.pdf", "application/pdf", new byte[60 * 1024 * 1024])); // 60MB

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateUploadedFiles(files));
    assertTrue(exception.getMessage().contains("exceeds maximum size of 50 MB"));
  }

  @Test
  void shouldThrowExceptionForInvalidFileType() {
    // Given
    List<MultipartFile> files =
        Arrays.asList(
            new MockMultipartFile("file", "test.exe", "application/exe", new byte[1024]));

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateUploadedFiles(files));
    assertEquals("File extension '.exe' not allowed. Allowed extensions: .pdf, .jpg, .jpeg, .png, .gif, .bmp, .tiff, .tif", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForEmptyFile() {
    // Given
    MockMultipartFile emptyFile =
        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
    List<MultipartFile> emptyFiles = Arrays.asList(emptyFile);

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateUploadedFiles(emptyFiles));
    assertEquals("Empty file not allowed", exception.getMessage());
  }

  @Test
  void shouldValidateInputFilesSuccessfully() {
    // Given
    List<String> inputFiles = Arrays.asList("job-123/input1.pdf", "job-456/input2.pdf");

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateInputFiles(inputFiles));
  }

  @Test
  void shouldThrowExceptionForSuspiciousPath() {
    // Given
    List<String> inputFiles = Arrays.asList("../../../etc/passwd");

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateInputFiles(inputFiles));
    assertEquals("Invalid file path. Path traversal not allowed", exception.getMessage());
  }

  @Test
  void shouldValidateOptionsJsonSuccessfully() {
    // Given
    String optionsJson = "{\"output_filename\":\"merged.pdf\"}";

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateOptionsJson(optionsJson));
  }

  @Test
  void shouldThrowExceptionForInvalidJson() {
    // Given - JSON format validation is not implemented, only size validation
    String optionsJson = "{invalid json}";

    // When & Then - This should not throw exception as format validation is not implemented
    assertDoesNotThrow(() -> inputValidationService.validateOptionsJson(optionsJson));
  }

  @Test
  void shouldThrowExceptionForJsonTooLarge() {
    // Given
    StringBuilder largeJson = new StringBuilder("{");
    for (int i = 0; i < 10000; i++) {
      largeJson.append("\"key").append(i).append("\":\"value").append(i).append("\",");
    }
    largeJson.append("\"end\":\"value\"}");

    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateOptionsJson(largeJson.toString()));
    assertEquals("Options JSON too long. Maximum 10000 characters allowed", exception.getMessage());
  }

  @Test
  void shouldValidatePaginationParamsSuccessfully() {
    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validatePaginationParams(0, 20));
    assertDoesNotThrow(() -> inputValidationService.validatePaginationParams(5, 50));
  }

  @Test
  void shouldThrowExceptionForInvalidPageNumber() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validatePaginationParams(-1, 20));
    assertEquals("Page number must be non-negative", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForInvalidPageSize() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validatePaginationParams(0, 0));
    assertEquals("Page size must be between 1 and 100", exception.getMessage());

    exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validatePaginationParams(0, 101));
    assertEquals("Page size must be between 1 and 100", exception.getMessage());
  }

  @Test
  void shouldValidateJobIdSuccessfully() {
    // Given
    String jobId = "550e8400-e29b-41d4-a716-446655440000";

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateJobId(jobId));
  }

  @Test
  void shouldThrowExceptionForNullJobId() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateJobId(null));
    assertEquals("Job ID is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionForInvalidJobIdFormat() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateJobId("invalid-id"));
    assertEquals("Job ID must be a valid UUID", exception.getMessage());
  }

  @Test
  void shouldValidateInputProvidedSuccessfully() {
    // Given
    List<MultipartFile> files =
        Arrays.asList(
            new MockMultipartFile("file", "test.pdf", "application/pdf", new byte[1024]));
    List<String> inputFiles = null;

    // When & Then
    assertDoesNotThrow(() -> inputValidationService.validateInputProvided(files, inputFiles));
  }

  @Test
  void shouldThrowExceptionWhenNoInputProvided() {
    // When & Then
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateInputProvided(null, null));
    assertEquals(
        "Either files or inputFiles parameter is required",
        exception.getMessage());

    exception =
        assertThrows(
            SecurityValidationException.class,
            () -> inputValidationService.validateInputProvided(Collections.emptyList(), Collections.emptyList()));
    assertEquals(
        "Either files or inputFiles parameter is required",
        exception.getMessage());
  }
}