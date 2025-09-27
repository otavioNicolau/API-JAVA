package com.pdfprocessor.application.dto;

import com.pdfprocessor.domain.model.JobOperation;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** DTO para requisição de criação de job. */
public class CreateJobRequest {

  private String jobId;

  @NotNull(message = "Operação é obrigatória") private JobOperation operation;

  private List<String> inputFiles;

  private Map<String, Object> options;

  public CreateJobRequest() {}

  public CreateJobRequest(
      JobOperation operation, List<String> inputFiles, Map<String, Object> options) {
    this.operation = operation;
    this.inputFiles = inputFiles;
    this.options = options;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public JobOperation getOperation() {
    return operation;
  }

  public void setOperation(JobOperation operation) {
    this.operation = operation;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CreateJobRequest that = (CreateJobRequest) o;
    return operation == that.operation
        && Objects.equals(inputFiles, that.inputFiles)
        && Objects.equals(options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operation, inputFiles, options);
  }

  @Override
  public String toString() {
    return "CreateJobRequest{"
        + "operation="
        + operation
        + ", inputFiles="
        + inputFiles
        + ", options="
        + options
        + '}';
  }
}
