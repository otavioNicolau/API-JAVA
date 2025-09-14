package com.pdfprocessor.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pdfprocessor.api.service.InputValidationService;
import com.pdfprocessor.api.service.RateLimitService;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.CreateJobUseCase;
import com.pdfprocessor.application.usecase.GetJobStatusUseCase;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private RateLimitService rateLimitService;

  @MockBean private InputValidationService inputValidationService;
  @MockBean private CreateJobUseCase createJobUseCase;
  @MockBean private GetJobStatusUseCase getJobStatusUseCase;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    // Limpar dados de rate limit antes de cada teste
    rateLimitService.clearRateLimitData();

    // Configurar mocks para não interferir nos testes de rate limit
    doNothing().when(inputValidationService).validateOperation(any());
    doNothing().when(inputValidationService).validateUploadedFiles(any());
    doNothing().when(inputValidationService).validateInputFiles(any());
    doNothing().when(inputValidationService).validateOptionsJson(any());

    // Mock do CreateJobUseCase para retornar JobResponse
    JobResponse mockResponse = new JobResponse();
    mockResponse.setId("test-job-id");
    mockResponse.setOperation(JobOperation.MERGE);
    mockResponse.setStatus(JobStatus.PENDING);
    when(createJobUseCase.execute(any())).thenReturn(mockResponse);
  }

  @Test
  void shouldAllowRequestsWithinRateLimit() throws Exception {
    String apiKey = "test-api-key-valid";
    MockMultipartFile file =
        new MockMultipartFile("files", "test.pdf", "application/pdf", "test content".getBytes());

    // Fazer 5 requisições (bem dentro do limite de 100/hora)
    for (int i = 0; i < 5; i++) {
      mockMvc
          .perform(
              multipart("/api/v1/jobs")
                  .file(file)
                  .param("operation", "merge")
                  .header("X-API-Key", apiKey)
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isOk());
    }
  }

  @Test
  void shouldRejectRequestsExceedingRateLimit() throws Exception {
    String apiKey = "test-api-key-limit";
    MockMultipartFile file =
        new MockMultipartFile("files", "test.pdf", "application/pdf", "test content".getBytes());

    // Simular 100 requisições fazendo chamadas diretas ao RateLimitService
    for (int i = 0; i < 100; i++) {
      rateLimitService.checkRateLimit(apiKey);
    }

    // A próxima requisição via HTTP deve ser rejeitada com 429 (Too Many Requests)
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file)
                .param("operation", "merge")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isTooManyRequests());
  }

  @Test
  void shouldHandleRateLimitPerApiKey() throws Exception {
    String apiKey1 = "test-api-key-1";
    String apiKey2 = "test-api-key-2";
    MockMultipartFile file =
        new MockMultipartFile("files", "test.pdf", "application/pdf", "test content".getBytes());

    // Simular 100 requisições para apiKey1
    for (int i = 0; i < 100; i++) {
      rateLimitService.checkRateLimit(apiKey1);
    }

    // apiKey1 deve ser rejeitada
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file)
                .param("operation", "merge")
                .header("X-API-Key", apiKey1)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isTooManyRequests());

    // apiKey2 deve ainda funcionar
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file)
                .param("operation", "merge")
                .header("X-API-Key", apiKey2)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnBadRequestWhenApiKeyMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("files", "test.pdf", "application/pdf", "test content".getBytes());

    // Requisição sem X-API-Key deve retornar 400 (Bad Request)
    mockMvc
        .perform(
            multipart("/api/v1/jobs")
                .file(file)
                .param("operation", "merge")
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest());
  }
}
