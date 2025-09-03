package com.pdfprocessor.application.dto;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.model.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** DTO para resposta de job. */
public class JobResponse {

  private String id;
  private JobOperation operation;
  private JobStatus status;
  private List<String> inputFiles;
  private Map<String, Object> options;
  private String errorMessage;
  private String resultPath;
  private Integer progress;
  private LocalDateTime createdAt;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;

  public JobResponse() {}

  public JobResponse(
      String id,
      JobOperation operation,
      JobStatus status,
      List<String> inputFiles,
      Map<String, Object> options,
      String errorMessage,
      String resultPath,
      Integer progress,
      LocalDateTime createdAt,
      LocalDateTime startedAt,
      LocalDateTime completedAt) {
    this.id = id;
    this.operation = operation;
    this.status = status;
    this.inputFiles = inputFiles;
    this.options = options;
    this.errorMessage = errorMessage;
    this.resultPath = resultPath;
    this.progress = progress;
    this.createdAt = createdAt;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
  }

  /** Cria um JobResponse a partir de um Job. */
  public static JobResponse fromJob(Job job) {
    return new JobResponse(
        job.getId(),
        job.getOperation(),
        job.getStatus(),
        job.getInputFiles(),
        job.getOptions(),
        job.getErrorMessage(),
        job.getResultPath(),
        job.getProgress(),
        job.getCreatedAt(),
        job.getStartedAt(),
        job.getCompletedAt());
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public JobOperation getOperation() {
    return operation;
  }

  public void setOperation(JobOperation operation) {
    this.operation = operation;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public List<String> getInputFiles() {
    return inputFiles;
  }

  public void setInputFiles(List<String> inputFiles) {
    this.inputFiles = inputFiles;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public void setOptions(Map<String, Object> options) {
    this.options = options;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getResultPath() {
    return resultPath;
  }

  public void setResultPath(String resultPath) {
    this.resultPath = resultPath;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(LocalDateTime completedAt) {
    this.completedAt = completedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    JobResponse that = (JobResponse) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "JobResponse{"
        + "id='"
        + id
        + '\''
        + ", operation="
        + operation
        + ", status="
        + status
        + ", progress="
        + progress
        + '}';
  }
}
