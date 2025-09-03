package com.pdfprocessor.domain.port;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;

/** Porta para serviço de processamento de PDF. */
public interface PdfProcessingService {

  /**
   * Processa um job de PDF.
   *
   * @param job o job a ser processado
   * @return caminho do arquivo resultado
   */
  String processJob(Job job);

  /**
   * Verifica se uma operação é suportada.
   *
   * @param operation a operação
   * @return true se a operação é suportada
   */
  boolean supportsOperation(JobOperation operation);

  /**
   * Obtém as operações suportadas.
   *
   * @return lista de operações suportadas
   */
  List<JobOperation> getSupportedOperations();

  /**
   * Valida as opções para uma operação específica.
   *
   * @param operation a operação
   * @param options as opções
   * @return true se as opções são válidas
   */
  boolean validateOptions(JobOperation operation, Map<String, Object> options);

  /**
   * Obtém o esquema de opções para uma operação.
   *
   * @param operation a operação
   * @return mapa com o esquema das opções
   */
  Map<String, Object> getOptionsSchema(JobOperation operation);
}
