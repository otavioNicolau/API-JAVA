package com.pdfprocessor.api.exception;

import com.pdfprocessor.domain.exception.JobNotCompletedException;
import com.pdfprocessor.domain.exception.JobNotFoundException;
import com.pdfprocessor.domain.exception.ResultNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Handler global para tratamento de exceções da API. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(JobNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleJobNotFoundException(JobNotFoundException ex) {
    ErrorResponse error = new ErrorResponse("JOB_NOT_FOUND", ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(JobNotCompletedException.class)
  public ResponseEntity<ErrorResponse> handleJobNotCompletedException(JobNotCompletedException ex) {
    ErrorResponse error =
        new ErrorResponse("JOB_NOT_COMPLETED", ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(ResultNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResultNotFoundException(ResultNotFoundException ex) {
    ErrorResponse error =
        new ErrorResponse("RESULT_NOT_FOUND", ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(SecurityValidationException.class)
  public ResponseEntity<ErrorResponse> handleSecurityValidationException(SecurityValidationException ex) {
    LOGGER.warn("Security validation failed: {} - {}", ex.getErrorCode(), ex.getMessage());
    ErrorResponse error =
        new ErrorResponse(ex.getErrorCode(), ex.getMessage(), LocalDateTime.now());
    
    // Retornar status HTTP apropriado baseado no tipo de erro
    HttpStatus status = switch (ex.getErrorCode()) {
      case "FILE_SIZE_EXCEEDED", "MAX_FILES_EXCEEDED" -> HttpStatus.PAYLOAD_TOO_LARGE;
      case "RATE_LIMIT_EXCEEDED" -> HttpStatus.TOO_MANY_REQUESTS;
      case "INVALID_API_KEY" -> HttpStatus.UNAUTHORIZED;
      default -> HttpStatus.BAD_REQUEST;
    };
    
    return ResponseEntity.status(status).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErrorResponse error =
        new ErrorResponse("INVALID_ARGUMENT", ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(
      UnsupportedOperationException ex) {
    ErrorResponse error =
        new ErrorResponse("UNSUPPORTED_OPERATION", ex.getMessage(), LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ErrorResponse error =
        new ErrorResponse("VALIDATION_ERROR", "Erro de validação", LocalDateTime.now(), errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOGGER.error("Erro interno não tratado: {}", ex.getMessage(), ex);
    ErrorResponse error =
        new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor", LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }

  public static class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse(String code, String message, LocalDateTime timestamp) {
      this.code = code;
      this.message = message;
      this.timestamp = timestamp;
    }

    public ErrorResponse(
        String code, String message, LocalDateTime timestamp, Map<String, String> details) {
      this.code = code;
      this.message = message;
      this.timestamp = timestamp;
      this.details = details;
    }

    // Getters
    public String getCode() {
      return code;
    }

    public String getMessage() {
      return message;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public Map<String, String> getDetails() {
      return details;
    }
  }
}
