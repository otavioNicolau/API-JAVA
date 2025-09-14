package com.pdfprocessor.api.service;

import com.pdfprocessor.api.exception.SecurityValidationException;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
// import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Serviço para validação rigorosa de parâmetros de entrada. Implementa validações de segurança para
 * prevenir ataques e garantir integridade dos dados.
 */
@Service
public class InputValidationService {

  // Constantes de validação
  private static final int MAX_FILENAME_LENGTH = 255;
  private static final int MAX_OPERATION_NAME_LENGTH = 50;
  private static final int MAX_OPTIONS_JSON_LENGTH = 10000;
  private static final int MAX_INPUT_FILES_COUNT = 10;
  private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024; // 50MB

  // Padrões de validação
  private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
  private static final Pattern SAFE_PATH_PATTERN = Pattern.compile("^[a-zA-Z0-9/._-]+$");
  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  // Lista de extensões permitidas
  private static final List<String> ALLOWED_FILE_EXTENSIONS =
      List.of(".pdf", ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".tif");

  // Lista de tipos MIME permitidos
  private static final List<String> ALLOWED_MIME_TYPES =
      List.of("application/pdf", "image/jpeg", "image/png", "image/gif", "image/bmp", "image/tiff");

  /**
   * Valida a operação fornecida.
   *
   * @param operation nome da operação
   * @throws SecurityValidationException se a operação for inválida
   */
  public void validateOperation(String operation) {
    if (operation == null || operation.trim().isEmpty()) {
      throw new SecurityValidationException("Operation parameter is required", "MISSING_OPERATION");
    }

    if (operation.length() > MAX_OPERATION_NAME_LENGTH) {
      throw new SecurityValidationException(
          String.format(
              "Operation name too long. Maximum %d characters allowed", MAX_OPERATION_NAME_LENGTH),
          "OPERATION_NAME_TOO_LONG");
    }

    // Verificar se contém apenas caracteres alfanuméricos e underscore
    if (!operation.matches("^[A-Z_]+$")) {
      throw new SecurityValidationException(
          "Operation name contains invalid characters. Only uppercase letters and underscores allowed",
          "INVALID_OPERATION_FORMAT");
    }

    // Verificar se a operação existe no enum
    try {
      JobOperation.valueOf(operation.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new SecurityValidationException(
          "Unsupported operation: " + operation, "UNSUPPORTED_OPERATION");
    }
  }

  /**
   * Valida arquivos enviados via upload.
   *
   * @param files lista de arquivos
   * @throws SecurityValidationException se algum arquivo for inválido
   */
  public void validateUploadedFiles(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return; // Pode ser válido se inputFiles for fornecido
    }

    if (files.size() > MAX_INPUT_FILES_COUNT) {
      throw new SecurityValidationException(
          String.format(
              "Maximum %d files allowed per job, but %d files provided",
              MAX_INPUT_FILES_COUNT, files.size()),
          "MAX_FILES_EXCEEDED");
    }

    for (MultipartFile file : files) {
      validateSingleFile(file);
    }
  }

  /**
   * Valida um único arquivo enviado.
   *
   * @param file arquivo a ser validado
   * @throws SecurityValidationException se o arquivo for inválido
   */
  private void validateSingleFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new SecurityValidationException("Empty file not allowed", "EMPTY_FILE");
    }

    // Validar tamanho do arquivo
    if (file.getSize() > MAX_FILE_SIZE_BYTES) {
      throw new SecurityValidationException(
          String.format(
              "File '%s' exceeds maximum size of %d MB. File size: %.2f MB",
              file.getOriginalFilename(),
              MAX_FILE_SIZE_BYTES / (1024 * 1024),
              file.getSize() / (1024.0 * 1024.0)),
          "FILE_SIZE_EXCEEDED");
    }

    // Validar nome do arquivo
    String filename = file.getOriginalFilename();
    if (filename == null || filename.trim().isEmpty()) {
      throw new SecurityValidationException("Filename is required", "MISSING_FILENAME");
    }

    if (filename.length() > MAX_FILENAME_LENGTH) {
      throw new SecurityValidationException(
          String.format("Filename too long. Maximum %d characters allowed", MAX_FILENAME_LENGTH),
          "FILENAME_TOO_LONG");
    }

    // Validar caracteres do nome do arquivo
    if (!SAFE_FILENAME_PATTERN.matcher(filename).matches()) {
      throw new SecurityValidationException(
          "Filename contains invalid characters. Only alphanumeric, dots, hyphens and underscores allowed",
          "INVALID_FILENAME_FORMAT");
    }

    // Validar extensão do arquivo
    String extension = getFileExtension(filename).toLowerCase();
    if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
      throw new SecurityValidationException(
          String.format(
              "File extension '%s' not allowed. Allowed extensions: %s",
              extension, String.join(", ", ALLOWED_FILE_EXTENSIONS)),
          "INVALID_FILE_EXTENSION");
    }

    // Validar tipo MIME
    String contentType = file.getContentType();
    if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType)) {
      throw new SecurityValidationException(
          String.format(
              "Content type '%s' not allowed. Allowed types: %s",
              contentType, String.join(", ", ALLOWED_MIME_TYPES)),
          "INVALID_CONTENT_TYPE");
    }
  }

  /**
   * Valida lista de arquivos existentes.
   *
   * @param inputFiles lista de caminhos de arquivos
   * @throws SecurityValidationException se algum caminho for inválido
   */
  public void validateInputFiles(List<String> inputFiles) {
    if (inputFiles == null || inputFiles.isEmpty()) {
      return; // Pode ser válido se files for fornecido
    }

    if (inputFiles.size() > MAX_INPUT_FILES_COUNT) {
      throw new SecurityValidationException(
          String.format(
              "Maximum %d files allowed per job, but %d files provided",
              MAX_INPUT_FILES_COUNT, inputFiles.size()),
          "MAX_FILES_EXCEEDED");
    }

    for (String filePath : inputFiles) {
      validateInputFilePath(filePath);
    }
  }

  /**
   * Valida um caminho de arquivo de entrada.
   *
   * @param filePath caminho do arquivo
   * @throws SecurityValidationException se o caminho for inválido
   */
  private void validateInputFilePath(String filePath) {
    if (filePath == null || filePath.trim().isEmpty()) {
      throw new SecurityValidationException("File path cannot be empty", "EMPTY_FILE_PATH");
    }

    // Prevenir path traversal attacks
    if (filePath.contains("..") || filePath.contains("~") || filePath.startsWith("/")) {
      throw new SecurityValidationException(
          "Invalid file path. Path traversal not allowed", "PATH_TRAVERSAL_DETECTED");
    }

    // Validar formato do caminho
    if (!SAFE_PATH_PATTERN.matcher(filePath).matches()) {
      throw new SecurityValidationException(
          "File path contains invalid characters", "INVALID_PATH_FORMAT");
    }

    // Validar extensão do arquivo no caminho
    String extension = getFileExtension(filePath).toLowerCase();
    if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
      throw new SecurityValidationException(
          String.format("File extension '%s' not allowed in path '%s'", extension, filePath),
          "INVALID_FILE_EXTENSION");
    }
  }

  /**
   * Valida o JSON de opções.
   *
   * @param optionsJson string JSON com opções
   * @throws SecurityValidationException se o JSON for inválido
   */
  public void validateOptionsJson(String optionsJson) {
    if (optionsJson == null || optionsJson.trim().isEmpty()) {
      return; // Opções são opcionais
    }

    if (optionsJson.length() > MAX_OPTIONS_JSON_LENGTH) {
      throw new SecurityValidationException(
          String.format(
              "Options JSON too long. Maximum %d characters allowed", MAX_OPTIONS_JSON_LENGTH),
          "OPTIONS_JSON_TOO_LONG");
    }

    // Validar caracteres básicos do JSON
    String trimmed = optionsJson.trim();
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
      throw new SecurityValidationException(
          "Options must be a valid JSON object", "INVALID_JSON_FORMAT");
    }
  }

  /**
   * Valida parâmetros de paginação.
   *
   * @param page número da página
   * @param size tamanho da página
   * @throws SecurityValidationException se os parâmetros forem inválidos
   */
  public void validatePaginationParams(int page, int size) {
    if (page < 0) {
      throw new SecurityValidationException(
          "Page number must be non-negative", "INVALID_PAGE_NUMBER");
    }

    if (size < 1 || size > 100) {
      throw new SecurityValidationException(
          "Page size must be between 1 and 100", "INVALID_PAGE_SIZE");
    }
  }

  /**
   * Valida ID de job.
   *
   * @param jobId ID do job
   * @throws SecurityValidationException se o ID for inválido
   */
  public void validateJobId(String jobId) {
    if (jobId == null || jobId.trim().isEmpty()) {
      throw new SecurityValidationException("Job ID is required", "MISSING_JOB_ID");
    }

    if (!UUID_PATTERN.matcher(jobId).matches()) {
      throw new SecurityValidationException("Job ID must be a valid UUID", "INVALID_JOB_ID_FORMAT");
    }
  }

  /**
   * Valida se pelo menos um tipo de entrada foi fornecido.
   *
   * @param files arquivos enviados
   * @param inputFiles caminhos de arquivos existentes
   * @throws SecurityValidationException se nenhuma entrada for fornecida
   */
  public void validateInputProvided(List<MultipartFile> files, List<String> inputFiles) {
    boolean hasFiles =
        files != null && !files.isEmpty() && files.stream().anyMatch(f -> !f.isEmpty());
    boolean hasInputFiles = inputFiles != null && !inputFiles.isEmpty();

    if (!hasFiles && !hasInputFiles) {
      throw new SecurityValidationException(
          "Either files or inputFiles parameter is required", "NO_INPUT_PROVIDED");
    }

    if (hasFiles && hasInputFiles) {
      throw new SecurityValidationException(
          "Cannot provide both files and inputFiles parameters simultaneously",
          "CONFLICTING_INPUT_TYPES");
    }
  }

  /**
   * Extrai a extensão de um arquivo.
   *
   * @param filename nome do arquivo
   * @return extensão do arquivo (incluindo o ponto)
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
      return "";
    }
    return filename.substring(lastDotIndex);
  }
}
