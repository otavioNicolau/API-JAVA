package com.pdfprocessor.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.CreateJobUseCase;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import com.pdfprocessor.domain.port.StorageService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

/** Testes para validações de segurança no JobController. */
@WebMvcTest(JobController.class)
class SecurityValidationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateJobUseCase createJobUseCase;
  @MockBean private StorageService storageService;

  @Test
  void shouldRejectFileExceedingMaxSize() throws Exception {
    // Given - arquivo de 60MB (excede limite de 50MB)
    byte[] largeFileContent = new byte[60 * 1024 * 1024]; // 60MB
    MockMultipartFile largeFile =
        new MockMultipartFile("files", "large-file.pdf", "application/pdf", largeFileContent);

    // When & Then
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(largeFile)
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE"))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(jsonPath("$.code").value("FILE_SIZE_EXCEEDED"))
        .andExpect(
            jsonPath("$.message")
                .value("File 'large-file.pdf' exceeds maximum size of 50 MB. File size: 60.00 MB"));
  }

  @Test
  void shouldRejectTooManyFiles() throws Exception {
    // Given - 11 arquivos (excede limite de 10)
    MockMultipartFile file1 =
        new MockMultipartFile("files", "file1.pdf", "application/pdf", "content1".getBytes());
    MockMultipartFile file2 =
        new MockMultipartFile("files", "file2.pdf", "application/pdf", "content2".getBytes());
    MockMultipartFile file3 =
        new MockMultipartFile("files", "file3.pdf", "application/pdf", "content3".getBytes());
    MockMultipartFile file4 =
        new MockMultipartFile("files", "file4.pdf", "application/pdf", "content4".getBytes());
    MockMultipartFile file5 =
        new MockMultipartFile("files", "file5.pdf", "application/pdf", "content5".getBytes());
    MockMultipartFile file6 =
        new MockMultipartFile("files", "file6.pdf", "application/pdf", "content6".getBytes());
    MockMultipartFile file7 =
        new MockMultipartFile("files", "file7.pdf", "application/pdf", "content7".getBytes());
    MockMultipartFile file8 =
        new MockMultipartFile("files", "file8.pdf", "application/pdf", "content8".getBytes());
    MockMultipartFile file9 =
        new MockMultipartFile("files", "file9.pdf", "application/pdf", "content9".getBytes());
    MockMultipartFile file10 =
        new MockMultipartFile("files", "file10.pdf", "application/pdf", "content10".getBytes());
    MockMultipartFile file11 =
        new MockMultipartFile("files", "file11.pdf", "application/pdf", "content11".getBytes());

    // When & Then
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file1)
                .file(file2)
                .file(file3)
                .file(file4)
                .file(file5)
                .file(file6)
                .file(file7)
                .file(file8)
                .file(file9)
                .file(file10)
                .file(file11)
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE"))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(jsonPath("$.code").value("MAX_FILES_EXCEEDED"))
        .andExpect(
            jsonPath("$.message").value("Maximum 10 files allowed per job, but 11 files provided"));
  }

  @Test
  void shouldRejectTooManyInputFiles() throws Exception {
    // Given - 11 arquivos existentes (excede limite de 10)
    List<String> inputFiles =
        List.of(
            "job-1/file1.pdf",
            "job-1/file2.pdf",
            "job-1/file3.pdf",
            "job-1/file4.pdf",
            "job-1/file5.pdf",
            "job-1/file6.pdf",
            "job-1/file7.pdf",
            "job-1/file8.pdf",
            "job-1/file9.pdf",
            "job-1/file10.pdf",
            "job-1/file11.pdf");

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE")
                .param("inputFiles", inputFiles.toArray(new String[0])))
        .andExpect(status().isPayloadTooLarge())
        .andExpect(jsonPath("$.code").value("MAX_FILES_EXCEEDED"))
        .andExpect(
            jsonPath("$.message").value("Maximum 10 files allowed per job, but 11 files provided"));
  }

  @Test
  void shouldAcceptValidFileSize() throws Exception {
    // Given - arquivo de 30MB (dentro do limite de 50MB)
    byte[] validFileContent = new byte[30 * 1024 * 1024]; // 30MB
    MockMultipartFile validFile =
        new MockMultipartFile("files", "valid-file.pdf", "application/pdf", validFileContent);

    // Mock dependencies
    when(storageService.store(anyString(), anyString(), any()))
        .thenReturn("job-123/valid-file.pdf");
    when(createJobUseCase.execute(any())).thenReturn(createMockJobResponse());

    // When & Then
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(validFile)
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-job-123"));
  }

  @Test
  void shouldAcceptValidNumberOfFiles() throws Exception {
    // Given - 5 arquivos (dentro do limite de 10)
    MockMultipartFile file1 =
        new MockMultipartFile("files", "file1.pdf", "application/pdf", "content1".getBytes());
    MockMultipartFile file2 =
        new MockMultipartFile("files", "file2.pdf", "application/pdf", "content2".getBytes());
    MockMultipartFile file3 =
        new MockMultipartFile("files", "file3.pdf", "application/pdf", "content3".getBytes());
    MockMultipartFile file4 =
        new MockMultipartFile("files", "file4.pdf", "application/pdf", "content4".getBytes());
    MockMultipartFile file5 =
        new MockMultipartFile("files", "file5.pdf", "application/pdf", "content5".getBytes());

    // Mock dependencies
    when(storageService.store(anyString(), anyString(), any())).thenReturn("job-123/file.pdf");
    when(createJobUseCase.execute(any())).thenReturn(createMockJobResponse());

    // When & Then
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file1)
                .file(file2)
                .file(file3)
                .file(file4)
                .file(file5)
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("test-job-123"));
  }

  private JobResponse createMockJobResponse() {
    JobResponse response = new JobResponse();
    response.setId("test-job-123");
    response.setOperation(JobOperation.MERGE);
    response.setStatus(JobStatus.PENDING);
    response.setCreatedAt(LocalDateTime.now());
    response.setInputFiles(Collections.singletonList("job-123/input.pdf"));
    response.setProgress(0);
    return response;
  }
}
