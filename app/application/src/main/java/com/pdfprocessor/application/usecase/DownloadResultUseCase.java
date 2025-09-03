package com.pdfprocessor.application.usecase;

import com.pdfprocessor.domain.exception.JobNotCompletedException;
import com.pdfprocessor.domain.exception.JobNotFoundException;
import com.pdfprocessor.domain.exception.ResultNotFoundException;
import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobStatus;
import com.pdfprocessor.domain.port.JobRepository;
import com.pdfprocessor.domain.port.StorageService;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** Caso de uso para download do resultado de um job. */
@Service
public class DownloadResultUseCase {

  private final JobRepository jobRepository;
  private final StorageService storageService;

  public DownloadResultUseCase(JobRepository jobRepository, StorageService storageService) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
    this.storageService = Objects.requireNonNull(storageService);
  }

  /**
   * Executa o caso de uso de download do resultado.
   *
   * @param jobId ID do job
   * @return resposta com dados do arquivo
   * @throws JobNotFoundException se o job não for encontrado
   * @throws JobNotCompletedException se o job não estiver completo
   * @throws ResultNotFoundException se o arquivo resultado não for encontrado
   */
  public DownloadResponse execute(String jobId) {
    if (jobId == null || jobId.trim().isEmpty()) {
      throw new IllegalArgumentException("Job ID cannot be null or empty");
    }

    Job job =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));

    if (job.getStatus() != JobStatus.COMPLETED) {
      throw new JobNotCompletedException("Job is not completed yet: " + jobId);
    }

    if (job.getResultPath() == null) {
      throw new ResultNotFoundException("No result file found for job: " + jobId);
    }

    if (!storageService.exists(job.getResultPath())) {
      throw new ResultNotFoundException("Result file not found: " + job.getResultPath());
    }

    InputStream fileStream = storageService.retrieve(job.getResultPath());
    long fileSize = storageService.getFileSize(job.getResultPath());
    String filename = extractFilename(job.getResultPath());

    return new DownloadResponse(fileStream, filename, fileSize, getContentType(filename));
  }

  private String extractFilename(String filePath) {
    int lastSlash = filePath.lastIndexOf('/');
    return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
  }

  private String getContentType(String filename) {
    String extension = getFileExtension(filename).toLowerCase();
    return switch (extension) {
      case "pdf" -> "application/pdf";
      case "zip" -> "application/zip";
      case "png" -> "image/png";
      case "jpg", "jpeg" -> "image/jpeg";
      case "tiff", "tif" -> "image/tiff";
      case "txt" -> "text/plain";
      case "json" -> "application/json";
      case "mp3" -> "audio/mpeg";
      case "wav" -> "audio/wav";
      default -> "application/octet-stream";
    };
  }

  private String getFileExtension(String filename) {
    int lastDot = filename.lastIndexOf('.');
    return lastDot >= 0 ? filename.substring(lastDot + 1) : "";
  }

  /** Resposta do caso de uso de download. */
  public static class DownloadResponse {
    private final InputStream fileStream;
    private final String filename;
    private final long fileSize;
    private final String contentType;

    public DownloadResponse(
        InputStream fileStream, String filename, long fileSize, String contentType) {
      this.fileStream = fileStream;
      this.filename = filename;
      this.fileSize = fileSize;
      this.contentType = contentType;
    }

    public InputStream getFileStream() {
      return fileStream;
    }

    public String getFilename() {
      return filename;
    }

    public long getFileSize() {
      return fileSize;
    }

    public String getContentType() {
      return contentType;
    }
  }
}
