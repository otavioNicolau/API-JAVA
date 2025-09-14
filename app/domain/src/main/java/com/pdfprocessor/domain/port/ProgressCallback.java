package com.pdfprocessor.domain.port;

/** Interface para callback de progresso durante o processamento de jobs. */
public interface ProgressCallback {

  /**
   * Reporta o progresso atual do job.
   *
   * @param jobId ID do job
   * @param progress Progresso atual (0-100)
   * @param message Mensagem opcional descrevendo o estado atual
   */
  void onProgress(String jobId, int progress, String message);

  /**
   * Reporta que o job foi concluído com sucesso.
   *
   * @param jobId ID do job
   * @param resultPath Caminho do arquivo resultado
   */
  void onCompleted(String jobId, String resultPath);

  /**
   * Reporta que o job falhou.
   *
   * @param jobId ID do job
   * @param error Erro que causou a falha
   */
  void onError(String jobId, Throwable error);
}
