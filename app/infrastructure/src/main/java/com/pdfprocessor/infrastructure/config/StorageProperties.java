package com.pdfprocessor.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Propriedades de configuração para o serviço de storage. */
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

  private String basePath = "./storage";

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }
}
