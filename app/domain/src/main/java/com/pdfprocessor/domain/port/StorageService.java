package com.pdfprocessor.domain.port;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/** Porta para serviço de armazenamento de arquivos. */
public interface StorageService {

  /**
   * Armazena um arquivo.
   *
   * @param jobId ID do job associado
   * @param filename nome do arquivo
   * @param inputStream conteúdo do arquivo
   * @return caminho onde o arquivo foi armazenado
   */
  String store(String jobId, String filename, InputStream inputStream);

  /**
   * Recupera um arquivo como InputStream.
   *
   * @param filePath caminho do arquivo
   * @return InputStream do arquivo
   */
  InputStream retrieve(String filePath);

  /**
   * Obtém o caminho físico de um arquivo.
   *
   * @param filePath caminho lógico do arquivo
   * @return Path físico do arquivo
   */
  Path getPhysicalPath(String filePath);

  /**
   * Lista todos os arquivos de um job.
   *
   * @param jobId ID do job
   * @return lista de caminhos dos arquivos
   */
  List<String> listJobFiles(String jobId);

  /**
   * Remove um arquivo.
   *
   * @param filePath caminho do arquivo
   * @return true se o arquivo foi removido
   */
  boolean delete(String filePath);

  /**
   * Remove todos os arquivos de um job.
   *
   * @param jobId ID do job
   * @return número de arquivos removidos
   */
  int deleteJobFiles(String jobId);

  /**
   * Verifica se um arquivo existe.
   *
   * @param filePath caminho do arquivo
   * @return true se o arquivo existe
   */
  boolean exists(String filePath);

  /**
   * Obtém o tamanho de um arquivo em bytes.
   *
   * @param filePath caminho do arquivo
   * @return tamanho do arquivo
   */
  long getFileSize(String filePath);

  /**
   * Cria um diretório para um job.
   *
   * @param jobId ID do job
   * @return caminho do diretório criado
   */
  String createJobDirectory(String jobId);
}
