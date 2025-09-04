package com.pdfprocessor.domain.port;

import com.pdfprocessor.domain.port.ProgressCallback;

/**
 * Serviço para notificação de progresso de jobs.
 * Esta interface permite desacoplar o worker do módulo API.
 */
public interface ProgressNotificationService extends ProgressCallback {
    
    /**
     * Registra um callback para um job específico.
     * 
     * @param jobId ID do job
     * @param callback callback a ser registrado
     */
    void registerCallback(String jobId, ProgressCallback callback);
    
    /**
     * Remove o callback de um job específico.
     * 
     * @param jobId ID do job
     */
    void removeCallback(String jobId);
}