package com.pdfprocessor.api.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfprocessor.api.config.ApiKeyAuthenticationFilter;
import com.pdfprocessor.api.config.SecurityConfig;
import com.pdfprocessor.application.dto.CreateJobRequest;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.CreateJobUseCase;
import com.pdfprocessor.application.usecase.GetJobStatusUseCase;
import com.pdfprocessor.application.usecase.ListAllJobsUseCase;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JobController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "app.security.api-keys[0]=test-key-67890",
    "app.security.api-keys[1]=dev-key-12345"
})
class JobControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CreateJobUseCase createJobUseCase;
  @MockBean private GetJobStatusUseCase getJobStatusUseCase;
  @MockBean private ListAllJobsUseCase listAllJobsUseCase;
  @MockBean private com.pdfprocessor.application.usecase.CancelJobUseCase cancelJobUseCase;
  @MockBean private com.pdfprocessor.application.usecase.DownloadResultUseCase downloadResultUseCase;
  @MockBean private com.pdfprocessor.domain.port.StorageService storageService;
  @MockBean private com.pdfprocessor.api.service.RateLimitService rateLimitService;
  @MockBean private com.pdfprocessor.api.service.InputValidationService inputValidationService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateJobSuccessfully() throws Exception {
    // Given
    JobResponse response = new JobResponse();
    response.setId("job-123");
    response.setOperation(JobOperation.MERGE);
    response.setStatus(JobStatus.PENDING);
    response.setInputFiles(Arrays.asList("file1.pdf", "file2.pdf"));
    response.setOptions(new HashMap<>());
    response.setCreatedAt(LocalDateTime.now());

    when(createJobUseCase.execute(any(CreateJobRequest.class))).thenReturn(response);

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .header("X-API-Key", "test-key-67890")
                .param("operation", "MERGE")
                .param("inputFiles", "file1.pdf", "file2.pdf"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("job-123"))
        .andExpect(jsonPath("$.operation").value("MERGE"))
        .andExpect(jsonPath("$.status").value("PENDING"));

    verify(createJobUseCase).execute(any(CreateJobRequest.class));
  }

  @Test
  void shouldGetJobStatusSuccessfully() throws Exception {
    // Given
    String jobId = "job-123";
    JobResponse response = new JobResponse();
    response.setId(jobId);
    response.setOperation(JobOperation.MERGE);
    response.setStatus(JobStatus.COMPLETED);
    response.setInputFiles(Arrays.asList("file1.pdf"));
    response.setOptions(new HashMap<>());
    response.setCreatedAt(LocalDateTime.now());
    response.setCompletedAt(LocalDateTime.now());
    response.setResultPath("./storage/job-123/result.pdf");

    when(getJobStatusUseCase.execute(jobId)).thenReturn(response);

    // When & Then
    mockMvc
        .perform(get("/api/v1/jobs/{jobId}", jobId).header("X-API-Key", "test-key-67890"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(jobId))
        .andExpect(jsonPath("$.operation").value("MERGE"))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.resultPath").value("./storage/job-123/result.pdf"));

    verify(getJobStatusUseCase).execute(jobId);
  }

  @Test
  void shouldReturnUnauthorizedWhenApiKeyMissing() throws Exception {
    // Given
    CreateJobRequest request = new CreateJobRequest();
    request.setOperation(JobOperation.MERGE);
    request.setInputFiles(Arrays.asList("file1.pdf"));
    request.setOptions(new HashMap<>());

    // When & Then
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    verify(createJobUseCase, never()).execute(any());
  }

  @Test
  void shouldReturnBadRequestForInvalidJob() throws Exception {
    // When & Then - Invalid operation parameter
    mockMvc
        .perform(
            post("/api/v1/jobs")
                .header("X-API-Key", "test-key-67890")
                .param("operation", "INVALID_OPERATION"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldListJobsSuccessfully() throws Exception {
    // Given
    JobResponse job1 = new JobResponse();
    job1.setId("job-1");
    job1.setOperation(JobOperation.MERGE);
    job1.setStatus(JobStatus.PENDING);
    job1.setInputFiles(Arrays.asList("file1.pdf"));
    job1.setOptions(new HashMap<>());
    job1.setCreatedAt(LocalDateTime.now());
    
    JobResponse job2 = new JobResponse();
    job2.setId("job-2");
    job2.setOperation(JobOperation.SPLIT);
    job2.setStatus(JobStatus.COMPLETED);
    job2.setInputFiles(Arrays.asList("file2.pdf"));
    job2.setOptions(new HashMap<>());
    job2.setCreatedAt(LocalDateTime.now());
    job2.setCompletedAt(LocalDateTime.now());

    when(listAllJobsUseCase.execute(0, 20)).thenReturn(Arrays.asList(job1, job2));

    // When & Then
    mockMvc
        .perform(get("/api/v1/jobs").header("X-API-Key", "test-key-67890"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").value("job-1"))
        .andExpect(jsonPath("$[1].id").value("job-2"));

    verify(listAllJobsUseCase).execute(0, 20);
  }
}