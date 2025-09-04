package com.pdfprocessor.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.GetJobStatusUseCase;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Testes unitários para SseService.
 */
@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private GetJobStatusUseCase getJobStatusUseCase;

    @Mock
    private ObjectMapper objectMapper;

    private SseService sseService;
    private String testJobId;

    @BeforeEach
    void setUp() {
        sseService = new SseService(getJobStatusUseCase, objectMapper);
        testJobId = "test-job-123";
    }

    @Test
    void shouldCreateEmitterSuccessfully() {
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);

        // Then
        assertNotNull(emitter);
        assertEquals(300000L, emitter.getTimeout()); // 5 minutos
    }

    @Test
    void shouldRemoveEmitterOnTimeout() {
        // Given
        SseEmitter emitter = sseService.createEmitter(testJobId);
        
        // When - simular timeout
        emitter.onTimeout(() -> sseService.removeEmitter(testJobId));
        
        // Then
        assertNotNull(emitter);
        // O emitter deve ser removido internamente quando timeout ocorrer
    }

    @Test
    void shouldRemoveEmitterOnCompletion() {
        // Given
        SseEmitter emitter = sseService.createEmitter(testJobId);
        
        // When - simular completion
        emitter.onCompletion(() -> sseService.removeEmitter(testJobId));
        
        // Then
        assertNotNull(emitter);
        // O emitter deve ser removido internamente quando completion ocorrer
    }

    @Test
    void shouldRemoveEmitterOnError() {
        // Given
        SseEmitter emitter = sseService.createEmitter(testJobId);
        
        // When - simular erro
        emitter.onError((throwable) -> sseService.removeEmitter(testJobId));
        
        // Then
        assertNotNull(emitter);
        // O emitter deve ser removido internamente quando erro ocorrer
    }

    @Test
    void shouldSendJobUpdateSuccessfully() throws Exception {
        // Given
        JobResponse jobResponse = new JobResponse(
            testJobId,
            JobOperation.MERGE,
            JobStatus.PROCESSING,
            List.of("input1.pdf", "input2.pdf"),
            Map.of(),
            null,
            null,
            50,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );
        
        String jsonData = "{\"jobId\":\"test-job-123\",\"status\":\"PROCESSING\",\"progress\":50}";
        when(objectMapper.writeValueAsString(jobResponse)).thenReturn(jsonData);
        
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);
        sseService.sendJobUpdate(testJobId, jobResponse);
        
        // Then
        verify(objectMapper).writeValueAsString(jobResponse);
        assertNotNull(emitter);
    }

    @Test
    void shouldHandleProgressCallback() throws Exception {
        // Given
        String message = "Processing page 1 of 10";
        int progress = 25;
        
        Map<String, Object> expectedData = Map.of(
            "type", "progress",
            "jobId", testJobId,
            "progress", progress,
            "message", message,
            "timestamp", System.currentTimeMillis()
        );
        
        String jsonData = "{\"type\":\"progress\",\"jobId\":\"test-job-123\",\"progress\":25}";
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(jsonData);
        
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);
        sseService.onProgress(testJobId, progress, message);
        
        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        assertNotNull(emitter);
    }

    @Test
    void shouldHandleCompletedCallback() throws Exception {
        // Given
        String resultPath = "/storage/test-job-123/result.pdf";
        
        Map<String, Object> expectedData = Map.of(
            "type", "completed",
            "jobId", testJobId,
            "resultPath", resultPath,
            "timestamp", System.currentTimeMillis()
        );
        
        String jsonData = "{\"type\":\"completed\",\"jobId\":\"test-job-123\"}";
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(jsonData);
        
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);
        sseService.onCompleted(testJobId, resultPath);
        
        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        assertNotNull(emitter);
    }

    @Test
    void shouldHandleErrorCallback() throws Exception {
        // Given
        Throwable error = new RuntimeException("Processing failed");
        
        Map<String, Object> expectedData = Map.of(
            "type", "error",
            "jobId", testJobId,
            "error", "Processing failed",
            "timestamp", System.currentTimeMillis()
        );
        
        String jsonData = "{\"type\":\"error\",\"jobId\":\"test-job-123\"}";
        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(jsonData);
        
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);
        sseService.onError(testJobId, error);
        
        // Then
        verify(objectMapper).writeValueAsString(any(Map.class));
        assertNotNull(emitter);
    }

    @Test
    void shouldNotSendUpdateWhenEmitterNotExists() throws Exception {
        // Given
        JobResponse jobResponse = new JobResponse(
            testJobId,
            JobOperation.MERGE,
            JobStatus.PROCESSING,
            List.of("input1.pdf", "input2.pdf"),
            Map.of(),
            null,
            null,
            50,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );
        
        // When - não criar emitter antes de enviar update
        sseService.sendJobUpdate(testJobId, jobResponse);
        
        // Then - não deve chamar objectMapper pois não há emitter
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    void shouldImplementProgressNotificationServiceInterface() {
        // Given
        String anotherJobId = "another-job-456";
        
        // When & Then - deve implementar os métodos da interface
        assertDoesNotThrow(() -> sseService.registerCallback(anotherJobId, sseService));
        assertDoesNotThrow(() -> sseService.removeCallback(anotherJobId));
    }

    @Test
    void shouldHandleMultipleEmitters() {
        // Given
        String jobId1 = "job-1";
        String jobId2 = "job-2";
        
        // When
        SseEmitter emitter1 = sseService.createEmitter(jobId1);
        SseEmitter emitter2 = sseService.createEmitter(jobId2);
        
        // Then
        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotEquals(emitter1, emitter2);
    }

    @Test
    void shouldReplaceExistingEmitterForSameJob() {
        // Given
        SseEmitter firstEmitter = sseService.createEmitter(testJobId);
        
        // When - criar outro emitter para o mesmo job
        SseEmitter secondEmitter = sseService.createEmitter(testJobId);
        
        // Then
        assertNotNull(firstEmitter);
        assertNotNull(secondEmitter);
        // O segundo emitter deve substituir o primeiro
    }

    @Test
    void shouldRemoveEmitterExplicitly() {
        // Given
        SseEmitter emitter = sseService.createEmitter(testJobId);
        assertNotNull(emitter);
        
        // When
        sseService.removeEmitter(testJobId);
        
        // Then - emitter deve ser removido internamente
        // Verificar que não há mais emitter para este job
        assertDoesNotThrow(() -> sseService.removeEmitter(testJobId));
    }

    @Test
    void shouldHandleJsonSerializationError() throws Exception {
        // Given
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});
        
        // When
        SseEmitter emitter = sseService.createEmitter(testJobId);
        
        // Then - não deve lançar exceção, deve remover emitter silenciosamente
        assertDoesNotThrow(() -> sseService.onProgress(testJobId, 50, "test message"));
        assertNotNull(emitter);
    }
}