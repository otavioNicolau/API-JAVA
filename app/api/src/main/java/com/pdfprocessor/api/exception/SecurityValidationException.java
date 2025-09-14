package com.pdfprocessor.api.exception;

/**
 * Exceção lançada quando validações de segurança falham. Inclui validações de tamanho de arquivo,
 * limite de arquivos, rate limiting, etc.
 */
public class SecurityValidationException extends RuntimeException {

  private final String errorCode;

  public SecurityValidationException(String message) {
    super(message);
    this.errorCode = "SECURITY_VALIDATION_ERROR";
  }

  public SecurityValidationException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
  }

  public SecurityValidationException(String message, Throwable cause) {
    super(message, cause);
    this.errorCode = "SECURITY_VALIDATION_ERROR";
  }

  public SecurityValidationException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
