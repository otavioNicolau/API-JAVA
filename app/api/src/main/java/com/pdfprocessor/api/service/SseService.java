package com.pdfprocessor.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pdfprocessor.application.dto.JobResponse;
import com.pdfprocessor.application.usecase.GetJobStatusUseCase;
import com.pdfprocessor.domain.port.ProgressCallback;
import com.pdfprocessor.domain.port.ProgressNotificationService;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Serviço para gerenciar Server-Sent Events (SSE) para atualizações de progresso de jobs. */
@Service
public class SseService implements ProgressNotificationService {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private final GetJobStatusUseCase getJobStatusUseCase;
  private final ObjectMapper objectMapper;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

  public SseService(GetJobStatusUseCase getJobStatusUseCase, ObjectMapper objectMapper) {
    this.getJobStatusUseCase = getJobStatusUseCase;
    this.objectMapper = objectMapper;
  }

  /** Cria uma nova conexão SSE para um job específico. */
  public SseEmitter createEmitter(String jobId) {
    SseEmitter emitter = new SseEmitter(300000L); // 5 minutos timeout

    emitter.onCompletion(
        () -> {
          emitters.remove(jobId);
        });

    emitter.onTimeout(
        () -> {
          emitters.remove(jobId);
        });

    emitter.onError(
        (ex) -> {
          emitters.remove(jobId);
        });

    emitters.put(jobId, emitter);

    // Iniciar monitoramento do job
    startJobMonitoring(jobId);

    return emitter;
  }

  /** Inicia o monitoramento de um job específico. */
  private void startJobMonitoring(String jobId) {
    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            SseEmitter emitter = emitters.get(jobId);
            if (emitter == null) {
              return; // Emitter foi removido
            }

            JobResponse jobStatus = getJobStatusUseCase.execute(jobId);

            // Enviar evento de atualização
            SseEmitter.SseEventBuilder event =
                SseEmitter.event()
                    .name("job-update")
                    .data(objectMapper.writeValueAsString(jobStatus));

            emitter.send(event);

            // Se o job terminou (sucesso ou erro), parar o monitoramento
            if (jobStatus.getStatus().name().equals("COMPLETED")
                || jobStatus.getStatus().name().equals("FAILED")
                || jobStatus.getStatus().name().equals("CANCELLED")) {

              // Enviar evento final
              SseEmitter.SseEventBuilder finalEvent =
                  SseEmitter.event()
                      .name("job-finished")
                      .data(objectMapper.writeValueAsString(jobStatus));

              emitter.send(finalEvent);
              emitter.complete();
              emitters.remove(jobId);
            }

          } catch (IOException e) {
            // Erro ao enviar dados, remover emitter
            emitters.remove(jobId);
          } catch (Exception e) {
            // Job não encontrado ou outro erro, enviar evento de erro
            try {
              SseEmitter emitter = emitters.get(jobId);
              if (emitter != null) {
                SseEmitter.SseEventBuilder errorEvent =
                    SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"Job not found or error occurred\"}");

                emitter.send(errorEvent);
                emitter.completeWithError(e);
                emitters.remove(jobId);
              }
            } catch (IOException ioException) {
              emitters.remove(jobId);
            }
          }
        },
        0,
        2,
        TimeUnit.SECONDS); // Verificar a cada 2 segundos
  }

  /** Envia uma atualização manual para um job específico. */
  public void sendJobUpdate(String jobId, JobResponse jobStatus) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter != null) {
      try {
        SseEmitter.SseEventBuilder event =
            SseEmitter.event().name("job-update").data(objectMapper.writeValueAsString(jobStatus));

        emitter.send(event);
      } catch (IOException e) {
        emitters.remove(jobId);
      }
    }
  }

  /** Remove um emitter específico. */
  public void removeEmitter(String jobId) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter != null) {
      emitter.complete();
      emitters.remove(jobId);
    }
  }

  /** Obtém o número de conexões ativas. */
  public int getActiveConnections() {
    return emitters.size();
  }

  // Implementação da interface ProgressCallback

  @Override
  public void onProgress(String jobId, int progress, String message) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter != null) {
      try {
        Map<String, Object> progressData =
            Map.of(
                "type", "progress",
                "jobId", jobId,
                "progress", progress,
                "message", message,
                "timestamp", System.currentTimeMillis());
        String jsonData = objectMapper.writeValueAsString(progressData);
        emitter.send(SseEmitter.event().name("progress").data(jsonData));
      } catch (IOException e) {
        emitters.remove(jobId);
      }
    }
  }

  @Override
  public void onCompleted(String jobId, String resultPath) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter != null) {
      try {
        Map<String, Object> completedData =
            Map.of(
                "type",
                "completed",
                "jobId",
                jobId,
                "progress",
                100,
                "message",
                "Job concluído com sucesso",
                "resultPath",
                resultPath,
                "timestamp",
                System.currentTimeMillis());
        String jsonData = objectMapper.writeValueAsString(completedData);
        emitter.send(SseEmitter.event().name("completed").data(jsonData));
        emitter.complete();
      } catch (IOException e) {
        // Ignore error on completion
      } finally {
        emitters.remove(jobId);
      }
    }
  }

  @Override
  public void onError(String jobId, Throwable error) {
    SseEmitter emitter = emitters.get(jobId);
    if (emitter != null) {
      try {
        Map<String, Object> errorData =
            Map.of(
                "type",
                "error",
                "jobId",
                jobId,
                "message",
                "Erro no processamento: " + error.getMessage(),
                "error",
                error.getClass().getSimpleName(),
                "timestamp",
                System.currentTimeMillis());
        String jsonData = objectMapper.writeValueAsString(errorData);
        emitter.send(SseEmitter.event().name("error").data(jsonData));
        emitter.completeWithError(error);
      } catch (IOException e) {
        // Ignore error on error handling
      } finally {
        emitters.remove(jobId);
      }
    }
  }

  @Override
  public void registerCallback(String jobId, ProgressCallback callback) {
    // Para SSE, não precisamos registrar callbacks externos
    // pois o próprio SseService atua como callback
  }

  @Override
  public void removeCallback(String jobId) {
    removeEmitter(jobId);
  }
}
