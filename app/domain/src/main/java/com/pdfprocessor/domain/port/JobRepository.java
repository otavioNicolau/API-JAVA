package com.pdfprocessor.domain.port;

import com.pdfprocessor.domain.model.Job;
import java.util.List;
import java.util.Optional;

/** Porta para persistência de jobs. */
public interface JobRepository {

  /**
   * Salva um job.
   *
   * @param job o job a ser salvo
   * @return o job salvo
   */
  Job save(Job job);

  /**
   * Busca um job pelo ID.
   *
   * @param id o ID do job
   * @return o job encontrado ou empty se não existir
   */
  Optional<Job> findById(String id);

  /**
   * Lista todos os jobs com paginação.
   *
   * @param page número da página (0-based)
   * @param size tamanho da página
   * @return lista de jobs
   */
  List<Job> findAll(int page, int size);

  /**
   * Remove um job pelo ID.
   *
   * @param id o ID do job
   */
  void deleteById(String id);

  /**
   * Verifica se um job existe.
   *
   * @param id o ID do job
   * @return true se o job existe
   */
  boolean existsById(String id);
}
