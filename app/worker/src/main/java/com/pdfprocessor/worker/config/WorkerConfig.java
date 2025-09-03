package com.pdfprocessor.worker.config;

import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Configuração específica do Worker. */
@Configuration
@EnableAsync
public class WorkerConfig {

  /** Configuração do pool de threads para processamento assíncrono. */
  @Bean(name = "jobProcessorExecutor")
  public Executor jobProcessorExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("JobProcessor-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();
    return executor;
  }

  /** Propriedades de configuração do worker. */
  @Bean
  @ConfigurationProperties(prefix = "worker")
  public WorkerProperties workerProperties() {
    return new WorkerProperties();
  }

  /** Classe para propriedades de configuração do worker. */
  public static class WorkerProperties {
    private int pollingIntervalSeconds = 5;
    private int maxRetries = 3;
    private boolean enabled = true;

    public int getPollingIntervalSeconds() {
      return pollingIntervalSeconds;
    }

    public void setPollingIntervalSeconds(int pollingIntervalSeconds) {
      this.pollingIntervalSeconds = pollingIntervalSeconds;
    }

    public int getMaxRetries() {
      return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}
