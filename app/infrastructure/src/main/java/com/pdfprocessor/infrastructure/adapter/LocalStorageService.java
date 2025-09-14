package com.pdfprocessor.infrastructure.adapter;

import com.pdfprocessor.domain.port.StorageService;
import com.pdfprocessor.infrastructure.config.StorageProperties;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * Implementação do serviço de storage usando filesystem local. Armazena arquivos no diretório
 * configurado organizados por jobId.
 */
@Component
public class LocalStorageService implements StorageService {

  private final StorageProperties storageProperties;

  public LocalStorageService(StorageProperties storageProperties) {
    this.storageProperties = storageProperties;
  }

  @Override
  public String store(String jobId, String filename, InputStream inputStream) {
    try {
      // Criar diretório do job se não existir
      Path jobDir = Paths.get(storageProperties.getBasePath(), jobId);
      Files.createDirectories(jobDir);

      // Caminho completo do arquivo
      Path filePath = jobDir.resolve(filename);

      // Copiar o InputStream para o arquivo
      Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

      String relativePath = "./storage/" + jobId + "/" + filename;
      System.out.println("Stored file: " + relativePath);
      return relativePath;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file: " + filename + " for job: " + jobId, e);
    }
  }

  @Override
  public InputStream retrieve(String filePath) {
    try {
      Path path = Paths.get(filePath);
      if (!Files.exists(path)) {
        throw new RuntimeException("File not found: " + filePath);
      }
      System.out.println("Retrieved file: " + filePath);
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new RuntimeException("Failed to retrieve file: " + filePath, e);
    }
  }

  @Override
  public Path getPhysicalPath(String filePath) {
    // Se o caminho já é absoluto, retorna como está
    Path path = Paths.get(filePath);
    if (path.isAbsolute()) {
      return path;
    }
    
    // Se é um caminho relativo que começa com ./storage, substitui pelo base path configurado
    if (filePath.startsWith("./storage/")) {
      String relativePart = filePath.substring("./storage/".length());
      return Paths.get(storageProperties.getBasePath(), relativePart).toAbsolutePath();
    }
    
    // Para outros caminhos relativos, resolve baseado no base path
    return Paths.get(storageProperties.getBasePath()).resolve(filePath).toAbsolutePath();
  }

  @Override
  public List<String> listJobFiles(String jobId) {
    try {
      Path jobDir = Paths.get(storageProperties.getBasePath(), jobId);
      if (!Files.exists(jobDir)) {
        return List.of();
      }

      try (Stream<Path> files = Files.list(jobDir)) {
        List<String> fileList =
            files
                .filter(Files::isRegularFile)
                .map(path -> "./storage/" + jobId + "/" + path.getFileName().toString())
                .toList();
        System.out.println("Listed " + fileList.size() + " files for job: " + jobId);
        return fileList;
      }
    } catch (IOException e) {
      System.err.println("Failed to list files for job: " + jobId + ", " + e.getMessage());
      return List.of();
    }
  }

  @Override
  public boolean delete(String filePath) {
    try {
      Path path = Paths.get(filePath);
      boolean deleted = Files.deleteIfExists(path);
      if (deleted) {
        System.out.println("Deleted file: " + filePath);
      } else {
        System.out.println("File not found for deletion: " + filePath);
      }
      return deleted;
    } catch (IOException e) {
      System.err.println("Failed to delete file: " + filePath + ", " + e.getMessage());
      return false;
    }
  }

  @Override
  public int deleteJobFiles(String jobId) {
    try {
      Path jobDir = Paths.get(storageProperties.getBasePath(), jobId);
      if (!Files.exists(jobDir)) {
        System.out.println("Job directory not found: " + jobId);
        return 0;
      }

      int deletedCount = 0;
      try (Stream<Path> files = Files.list(jobDir)) {
        for (Path file : files.filter(Files::isRegularFile).toList()) {
          try {
            Files.delete(file);
            deletedCount++;
            System.out.println("Deleted file: " + file);
          } catch (IOException e) {
            System.err.println("Failed to delete file: " + file + ", " + e.getMessage());
          }
        }
      }

      // Tentar remover o diretório do job se estiver vazio
      try {
        Files.delete(jobDir);
        System.out.println("Deleted job directory: " + jobDir);
      } catch (IOException e) {
        System.out.println("Job directory not empty or failed to delete: " + jobDir);
      }

      System.out.println("Deleted " + deletedCount + " files for job: " + jobId);
      return deletedCount;
    } catch (IOException e) {
      System.err.println("Failed to delete files for job: " + jobId + ", " + e.getMessage());
      return 0;
    }
  }

  @Override
  public boolean exists(String filePath) {
    Path path = Paths.get(filePath);
    boolean exists = Files.exists(path);
    System.out.println("File " + filePath + " exists: " + exists);
    return exists;
  }

  @Override
  public long getFileSize(String filePath) {
    try {
      Path path = Paths.get(filePath);
      if (!Files.exists(path)) {
        return 0;
      }
      long size = Files.size(path);
      System.out.println("File " + filePath + " size: " + size + " bytes");
      return size;
    } catch (IOException e) {
      System.err.println("Failed to get file size: " + filePath + ", " + e.getMessage());
      return 0;
    }
  }

  @Override
  public String createJobDirectory(String jobId) {
    try {
      Path jobDir = Paths.get(storageProperties.getBasePath(), jobId);
      Files.createDirectories(jobDir);
      String jobDirPath = "./storage/" + jobId;
      System.out.println("Created job directory: " + jobDirPath);
      return jobDirPath;
    } catch (IOException e) {
      throw new RuntimeException("Failed to create job directory for: " + jobId, e);
    }
  }
}
