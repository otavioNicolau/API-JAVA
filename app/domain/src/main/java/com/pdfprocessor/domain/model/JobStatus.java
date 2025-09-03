package com.pdfprocessor.domain.model;

/** Status de um job de processamento de PDF. */
public enum JobStatus {
  /** Job criado e aguardando processamento */
  PENDING,

  /** Job em processamento */
  PROCESSING,

  /** Job concluído com sucesso */
  COMPLETED,

  /** Job falhou durante o processamento */
  FAILED,

  /** Job foi cancelado */
  CANCELLED
}
