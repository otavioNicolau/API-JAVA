package com.pdfprocessor.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Aplicação principal da API de processamento de PDF. */
@SpringBootApplication
@ComponentScan(
    basePackages = {
      "com.pdfprocessor.api",
      "com.pdfprocessor.application",
      "com.pdfprocessor.infrastructure"
    })
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }
}
