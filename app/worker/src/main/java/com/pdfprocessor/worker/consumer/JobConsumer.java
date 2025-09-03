package com.pdfprocessor.worker.consumer;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.port.JobQueue;
import com.pdfprocessor.worker.processor.JobProcessor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Consumer responsável por fazer polling da fila Redis e processar jobs. */
@Component
public class JobConsumer implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(JobConsumer.class);
  private static final int POLLING_INTERVAL_SECONDS = 5;

  private final JobQueue jobQueue;
  private final JobProcessor jobProcessor;
  private volatile boolean running = true;

  @Autowired
  public JobConsumer(JobQueue jobQueue, JobProcessor jobProcessor) {
    this.jobQueue = jobQueue;
    this.jobProcessor = jobProcessor;
  }

  @Override
  public void run(String... args) throws Exception {
    LOGGER.info("Worker iniciado. Aguardando jobs...");

    while (running) {
      try {
        // Tenta consumir um job da fila
        var optionalJob = jobQueue.consume();

        if (optionalJob.isPresent()) {
          Job job = optionalJob.get();
          LOGGER.info("Job encontrado: {}", job.getId());
          processJob(job);
        } else {
          // Se não há jobs, aguarda antes de tentar novamente
          Thread.sleep(TimeUnit.SECONDS.toMillis(POLLING_INTERVAL_SECONDS));
        }

      } catch (InterruptedException e) {
        logger.info("JobConsumer interrompido");
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        logger.error("Erro no JobConsumer: {}", e.getMessage(), e);
        // Aguarda um pouco antes de tentar novamente em caso de erro
        Thread.sleep(TimeUnit.SECONDS.toMillis(POLLING_INTERVAL_SECONDS));
      }
    }

    logger.info("JobConsumer finalizado");
  }

  private void processJob(Job job) {
    try {
      LOGGER.info("Processando job: {} - Operação: {}", job.getId(), job.getOperation());
      jobProcessor.process(job);
      LOGGER.info("Job processado com sucesso: {}", job.getId());
    } catch (Exception e) {
      LOGGER.error("Erro ao processar job {}: {}", job.getId(), e.getMessage(), e);
    }
  }

  /** Para o consumer graciosamente. */
  public void stop() {
    logger.info("Parando JobConsumer...");
    running = false;
  }
}
