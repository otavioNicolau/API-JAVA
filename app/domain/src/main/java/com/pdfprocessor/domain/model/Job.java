package com.pdfprocessor.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Entidade que representa um job de processamento de PDF. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
  private final String id;
  private final JobOperation operation;
  private final List<String> inputFiles;
  private final Map<String, Object> options;
  private final LocalDateTime createdAt;
  private JobStatus status;
  private String errorMessage;
  private String resultPath;
  private Integer progress;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;

  public Job(
      String id, JobOperation operation, List<String> inputFiles, Map<String, Object> options) {
    this.id = Objects.requireNonNull(id, "Job ID cannot be null");
    this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
    this.inputFiles = Objects.requireNonNull(inputFiles, "Input files cannot be null");
    this.options = options != null ? options : Map.of();
    this.createdAt = LocalDateTime.now();
    this.status = JobStatus.PENDING;
    this.progress = 0;
  }

  @JsonCreator
  public Job(
      @JsonProperty("id") String id,
      @JsonProperty("operation") JobOperation operation,
      @JsonProperty("inputFiles") List<String> inputFiles,
      @JsonProperty("options") Map<String, Object> options,
      @JsonProperty("createdAt") LocalDateTime createdAt,
      @JsonProperty("status") JobStatus status,
      @JsonProperty("errorMessage") String errorMessage,
      @JsonProperty("resultPath") String resultPath,
      @JsonProperty("progress") Integer progress,
      @JsonProperty("startedAt") LocalDateTime startedAt,
      @JsonProperty("completedAt") LocalDateTime completedAt) {
    this.id = Objects.requireNonNull(id, "Job ID cannot be null");
    this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
    this.inputFiles = Objects.requireNonNull(inputFiles, "Input files cannot be null");
    this.options = options != null ? options : Map.of();
    this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    this.status = status != null ? status : JobStatus.PENDING;
    this.errorMessage = errorMessage;
    this.resultPath = resultPath;
    this.progress = progress != null ? progress : 0;
    this.startedAt = startedAt;
    this.completedAt = completedAt;
  }

  // Getters
  public String getId() {
    return id;
  }

  public JobOperation getOperation() {
    return operation;
  }

  public List<String> getInputFiles() {
    return inputFiles != null ? List.copyOf(inputFiles) : List.of();
  }

  public Map<String, Object> getOptions() {
    return options != null ? Map.copyOf(options) : Map.of();
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public JobStatus getStatus() {
    return status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getResultPath() {
    return resultPath;
  }

  public Integer getProgress() {
    return progress;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  // Business methods
  public void start() {
    if (this.status != JobStatus.PENDING) {
      throw new IllegalStateException("Job can only be started from PENDING status");
    }
    this.status = JobStatus.PROCESSING;
    this.startedAt = LocalDateTime.now();
  }

  public void updateProgress(int progress) {
    if (progress < 0 || progress > 100) {
      throw new IllegalArgumentException("Progress must be between 0 and 100");
    }
    this.progress = progress;
  }

  public void complete(String resultPath) {
    if (this.status != JobStatus.PROCESSING) {
      throw new IllegalStateException("Job can only be completed from PROCESSING status");
    }
    this.status = JobStatus.COMPLETED;
    this.resultPath = resultPath;
    this.progress = 100;
    this.completedAt = LocalDateTime.now();
  }

  public void fail(String errorMessage) {
    this.status = JobStatus.FAILED;
    this.errorMessage = errorMessage;
    this.completedAt = LocalDateTime.now();
  }

  public void cancel() {
    if (this.status == JobStatus.COMPLETED || this.status == JobStatus.FAILED) {
      throw new IllegalStateException("Cannot cancel a completed or failed job");
    }
    this.status = JobStatus.CANCELLED;
    this.completedAt = LocalDateTime.now();
  }

  public boolean isCompleted() {
    return status == JobStatus.COMPLETED;
  }

  public boolean isFailed() {
    return status == JobStatus.FAILED;
  }

  public boolean isProcessing() {
    return status == JobStatus.PROCESSING;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Job job = (Job) o;
    return Objects.equals(id, job.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Job{"
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
