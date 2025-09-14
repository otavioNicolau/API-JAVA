package com.pdfprocessor.worker.service;

import com.pdfprocessor.domain.port.ProgressCallback;
import com.pdfprocessor.domain.port.ProgressNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementação do ProgressNotificationService para o worker.
 * Esta implementação apenas registra os callbacks e logs o progresso.
 */
@Service
public class WorkerProgressNotificationService implements ProgressNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerProgressNotificationService.class);
    
    private final Map<String, ProgressCallback> callbacks = new ConcurrentHashMap<>();

    @Override
    public void registerCallback(String jobId, ProgressCallback callback) {
        callbacks.put(jobId, callback);
        LOGGER.debug("Callback registrado para job: {}", jobId);
    }

    @Override
    public void removeCallback(String jobId) {
        callbacks.remove(jobId);
        LOGGER.debug("Callback removido para job: {}", jobId);
    }

    @Override
    public void onProgress(String jobId, int progress, String message) {
        LOGGER.info("Job {} - Progresso: {}% - {}", jobId, progress, message);
        
        ProgressCallback callback = callbacks.get(jobId);
        if (callback != null) {
            callback.onProgress(jobId, progress, message);
        }
    }

    @Override
    public void onCompleted(String jobId, String resultPath) {
        LOGGER.info("Job {} concluído com sucesso. Resultado: {}", jobId, resultPath);
        
        ProgressCallback callback = callbacks.get(jobId);
        if (callback != null) {
            callback.onCompleted(jobId, resultPath);
        }
        
        // Remove o callback após conclusão
        removeCallback(jobId);
    }

    @Override
    public void onError(String jobId, Throwable error) {
        LOGGER.error("Job {} falhou com erro: {}", jobId, error.getMessage(), error);
        
        ProgressCallback callback = callbacks.get(jobId);
        if (callback != null) {
            callback.onError(jobId, error);
        }
        
        // Remove o callback após erro
        removeCallback(jobId);
    }
}