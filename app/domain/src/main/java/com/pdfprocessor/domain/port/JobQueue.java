package com.pdfprocessor.domain.port;

import com.pdfprocessor.domain.model.Job;
import java.util.Optional;

/** Porta para fila de jobs assíncronos. */
public interface JobQueue {

  /**
   * Publica um job na fila para processamento.
   *
   * @param job o job a ser processado
   */
  void publish(Job job);

  /**
   * Consome o próximo job da fila.
   *
   * @return o próximo job ou empty se não houver jobs
   */
  Optional<Job> consume();

  /**
   * Consome o próximo job da fila com timeout.
   *
   * @param timeoutSeconds tempo limite em segundos para aguardar um job
   * @return o próximo job ou empty se timeout
   */
  Optional<Job> consume(long timeoutSeconds);

  /**
   * Retorna um job para a fila (em caso de falha no processamento).
   *
   * @param job o job a ser retornado
   */
  void returnToQueue(Job job);

  /**
   * Confirma o processamento de um job (remove da fila).
   *
   * @param job o job processado
   */
  void acknowledge(Job job);

  /**
   * Obtém o tamanho atual da fila.
   *
   * @return número de jobs na fila
   */
  long getQueueSize();
}
