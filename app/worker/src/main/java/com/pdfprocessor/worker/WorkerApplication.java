package com.pdfprocessor.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/** Aplicação principal do Worker de processamento de PDF. */
@SpringBootApplication
@ComponentScan(
    basePackages = {
      "com.pdfprocessor.worker",
      "com.pdfprocessor.application",
      "com.pdfprocessor.infrastructure"
    })
public class WorkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkerApplication.class, args);
  }
}
