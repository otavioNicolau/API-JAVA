package com.pdfprocessor.domain.exception;

/** Exceção lançada quando o resultado de um job não é encontrado no storage. */
public class ResultNotFoundException extends RuntimeException {

  public ResultNotFoundException(String resultPath) {
    super("Resultado não encontrado: " + resultPath);
  }

  public ResultNotFoundException(String resultPath, Throwable cause) {
    super("Resultado não encontrado: " + resultPath, cause);
  }
}
