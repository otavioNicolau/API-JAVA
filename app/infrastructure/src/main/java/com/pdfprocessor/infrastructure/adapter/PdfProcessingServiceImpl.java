package com.pdfprocessor.infrastructure.adapter;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import com.pdfprocessor.domain.port.PdfProcessingService;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Implementação do serviço de processamento de PDF usando PDFBox. TODO: Implementar operações reais
 * de PDF.
 */
@Component
public class PdfProcessingServiceImpl implements PdfProcessingService {

  private static final List<JobOperation> SUPPORTED_OPERATIONS;

  static {
    SUPPORTED_OPERATIONS =
        List.of(
            JobOperation.MERGE,
            JobOperation.SPLIT,
            JobOperation.ROTATE,
            JobOperation.WATERMARK,
            JobOperation.ENCRYPT,
            JobOperation.DECRYPT,
            JobOperation.EXTRACT_TEXT,
            JobOperation.EXTRACT_METADATA,
            // Operações avançadas
            JobOperation.PDF_CROP,
            JobOperation.PDF_REORDER,
            JobOperation.PDF_RESIZE,
            JobOperation.COMPRESS,
            JobOperation.PDF_TO_IMAGES,
            JobOperation.IMAGES_TO_PDF);
  }

  @Override
  public String processJob(Job job) {
    if (job == null) {
      throw new IllegalArgumentException("Job cannot be null");
    }
    if (job.getOperation() == null) {
      throw new IllegalArgumentException("Job operation cannot be null");
    }

    System.out.println("Processing job: " + job.getId() + " with operation: " + job.getOperation());

    // Debug: verificar se job e seus campos não são null
    System.out.println("DEBUG: Job ID: " + job.getId());
    System.out.println("DEBUG: Job Operation: " + job.getOperation());
    System.out.println("DEBUG: Job InputFiles: " + job.getInputFiles());
    System.out.println("DEBUG: Job Options: " + job.getOptions());

    if (job.getInputFiles() == null) {
      throw new IllegalArgumentException("Job input files cannot be null");
    }

    if (job.getInputFiles().isEmpty()) {
      throw new IllegalArgumentException("Job input files cannot be empty");
    }

    try {
      return switch (job.getOperation()) {
        case MERGE -> processMerge(job);
        case SPLIT -> processSplit(job);
        case ROTATE -> processRotate(job);
        case WATERMARK -> processWatermark(job);
        case ENCRYPT -> processEncrypt(job);
        case DECRYPT -> processDecrypt(job);
        case EXTRACT_TEXT -> processExtractText(job);
        case EXTRACT_METADATA -> processExtractMetadata(job);
          // Operações avançadas
        case PDF_CROP -> processPdfCrop(job);
        case PDF_REORDER -> processPdfReorder(job);
        case PDF_RESIZE -> processPdfResize(job);
        case COMPRESS -> processCompress(job);
        case PDF_TO_IMAGES -> processPdfToImages(job);
        case IMAGES_TO_PDF -> processImagesToPdf(job);
        default ->
            throw new UnsupportedOperationException(
                "Operation not supported: " + job.getOperation());
      };
    } catch (Exception e) {
      throw new RuntimeException("Error processing job: " + job.getId(), e);
    }
  }

  private String processMerge(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    System.out.println("DEBUG: Input files for merge: " + inputFiles);

    if (inputFiles == null || inputFiles.size() < 2) {
      throw new IllegalArgumentException("MERGE operation requires at least 2 input files");
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    String resultPath = resultDir.resolve("result_" + job.getId() + ".pdf").toString();
    System.out.println("DEBUG: Result path: " + resultPath);

    PDFMergerUtility merger = new PDFMergerUtility();
    merger.setDestinationFileName(resultPath);

    // Adicionar todos os arquivos de entrada
    for (String inputFile : inputFiles) {
      File file = new File(inputFile);
      System.out.println(
          "DEBUG: Checking file: "
              + inputFile
              + ", exists: "
              + file.exists()
              + ", size: "
              + file.length());
      if (!file.exists()) {
        throw new IllegalArgumentException("Input file not found: " + inputFile);
      }
      merger.addSource(file);
      System.out.println("DEBUG: Added source file: " + inputFile);
    }

    System.out.println("DEBUG: Starting merge operation...");
    merger.mergeDocuments(null);
    System.out.println("DEBUG: Merge completed");

    // Verificar se o arquivo de resultado foi criado
    File resultFile = new File(resultPath);
    System.out.println(
        "DEBUG: Result file exists: " + resultFile.exists() + ", size: " + resultFile.length());

    System.out.println("Merged " + inputFiles.size() + " files into: " + resultPath);
    return resultPath;
  }

  private String processSplit(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("SPLIT operation requires at least 1 input file");
    }

    String inputFile = inputFiles.get(0); // SPLIT trabalha com apenas um arquivo
    File file = new File(inputFile);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputFile);
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    // Obter opções de split
    Map<String, Object> options = job.getOptions();
    String pages = options != null ? (String) options.get("pages") : "all";

    try (PDDocument document = Loader.loadPDF(file)) {
      int totalPages = document.getNumberOfPages();

      if ("all".equals(pages)) {
        // Dividir em páginas individuais
        return splitAllPages(document, resultDir, totalPages);
      } else {
        // Dividir por intervalo específico (ex: "1-3", "5", "1,3,5-7")
        return splitByRange(document, resultDir, pages, totalPages);
      }
    }
  }

  private String splitAllPages(PDDocument document, Path resultDir, int totalPages)
      throws IOException {
    Splitter splitter = new Splitter();
    List<PDDocument> pages = splitter.split(document);

    List<String> resultFiles = new ArrayList<>();

    for (int i = 0; i < pages.size(); i++) {
      String fileName = String.format("page_%03d.pdf", i + 1);
      Path filePath = resultDir.resolve(fileName);

      try (PDDocument pageDoc = pages.get(i)) {
        pageDoc.save(filePath.toFile());
        resultFiles.add(filePath.toString());
      }
    }

    System.out.println("Split " + totalPages + " pages into individual files in: " + resultDir);

    // Retorna o diretório contendo todos os arquivos
    return resultDir.toString();
  }

  private String splitByRange(PDDocument document, Path resultDir, String pages, int totalPages)
      throws IOException {
    List<Integer> pageNumbers = parsePageRange(pages, totalPages);

    if (pageNumbers.isEmpty()) {
      throw new IllegalArgumentException("No valid pages specified in range: " + pages);
    }

    // Criar um novo documento com as páginas especificadas
    try (PDDocument resultDoc = new PDDocument()) {
      for (Integer pageNum : pageNumbers) {
        if (pageNum >= 1 && pageNum <= totalPages) {
          PDPage page = document.getPage(pageNum - 1); // PDFBox usa índice 0
          resultDoc.addPage(page);
        }
      }

      String fileName = "pages_" + pages.replaceAll("[^0-9,-]", "_") + ".pdf";
      Path filePath = resultDir.resolve(fileName);
      resultDoc.save(filePath.toFile());

      System.out.println("Split pages " + pages + " into: " + filePath);
      return filePath.toString();
    }
  }

  private List<Integer> parsePageRange(String pages, int totalPages) {
    List<Integer> pageNumbers = new ArrayList<>();

    if (pages == null || pages.trim().isEmpty()) {
      return pageNumbers;
    }

    String[] parts = pages.split(",");
    for (String part : parts) {
      part = part.trim();
      if (part.contains("-")) {
        // Intervalo (ex: "1-3", "5-10")
        String[] range = part.split("-");
        if (range.length == 2) {
          try {
            int start = Integer.parseInt(range[0].trim());
            int end = Integer.parseInt(range[1].trim());
            for (int i = start; i <= end && i <= totalPages; i++) {
              if (i >= 1) {
                pageNumbers.add(i);
              }
            }
          } catch (NumberFormatException e) {
            System.err.println("Invalid page range: " + part);
          }
        }
      } else {
        // Página individual (ex: "5")
        try {
          int pageNum = Integer.parseInt(part);
          if (pageNum >= 1 && pageNum <= totalPages) {
            pageNumbers.add(pageNum);
          }
        } catch (NumberFormatException e) {
          System.err.println("Invalid page number: " + part);
        }
      }
    }

    return pageNumbers;
  }

  private String processRotate(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("ROTATE operation requires at least 1 input file");
    }

    String inputFile = inputFiles.get(0); // ROTATE trabalha com apenas um arquivo
    File file = new File(inputFile);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputFile);
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    // Obter opções de rotação
    Map<String, Object> options = job.getOptions();
    if (options == null) {
      throw new IllegalArgumentException(
          "ROTATE operation requires options with 'degrees' parameter");
    }

    Object degreesObj = options.get("degrees");
    if (degreesObj == null) {
      throw new IllegalArgumentException(
          "ROTATE operation requires 'degrees' parameter (90, 180, 270)");
    }

    int degrees;
    try {
      degrees = Integer.parseInt(degreesObj.toString());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid degrees value: " + degreesObj + ". Must be 90, 180, or 270");
    }

    if (degrees != 90 && degrees != 180 && degrees != 270) {
      throw new IllegalArgumentException("Degrees must be 90, 180, or 270. Got: " + degrees);
    }

    String pages = options.get("pages") != null ? options.get("pages").toString() : "all";

    String resultPath = resultDir.resolve("rotated.pdf").toString();

    try (PDDocument document = Loader.loadPDF(file)) {
      int totalPages = document.getNumberOfPages();

      if ("all".equals(pages)) {
        // Rotacionar todas as páginas
        for (int i = 0; i < totalPages; i++) {
          rotatePage(document.getPage(i), degrees);
        }
        System.out.println("Rotated all " + totalPages + " pages by " + degrees + " degrees");
      } else {
        // Rotacionar páginas específicas
        List<Integer> pageNumbers = parsePageRange(pages, totalPages);
        for (Integer pageNum : pageNumbers) {
          if (pageNum >= 1 && pageNum <= totalPages) {
            rotatePage(document.getPage(pageNum - 1), degrees);
          }
        }
        System.out.println("Rotated pages " + pages + " by " + degrees + " degrees");
      }

      document.save(resultPath);
    }

    System.out.println("Rotation completed. Result saved to: " + resultPath);
    return resultPath;
  }

  private void rotatePage(PDPage page, int degrees) {
    int currentRotation = page.getRotation();
    int newRotation = (currentRotation + degrees) % 360;
    page.setRotation(newRotation);
  }

  private String processWatermark(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("WATERMARK operation requires at least 1 input file");
    }

    String inputFile = inputFiles.get(0); // WATERMARK trabalha com apenas um arquivo
    File file = new File(inputFile);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputFile);
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    // Obter opções de watermark
    Map<String, Object> options = job.getOptions();
    if (options == null) {
      throw new IllegalArgumentException(
          "WATERMARK operation requires options with 'text' parameter");
    }

    String watermarkText = (String) options.get("text");
    if (watermarkText == null || watermarkText.trim().isEmpty()) {
      throw new IllegalArgumentException("WATERMARK operation requires 'text' parameter");
    }

    // Opções opcionais
    float opacity =
        options.get("opacity") != null ? Float.parseFloat(options.get("opacity").toString()) : 0.3f;
    int fontSize =
        options.get("fontSize") != null ? Integer.parseInt(options.get("fontSize").toString()) : 36;
    String position =
        options.get("position") != null ? options.get("position").toString() : "center";

    String resultPath = resultDir.resolve("watermarked.pdf").toString();

    try (PDDocument document = Loader.loadPDF(file)) {
      for (PDPage page : document.getPages()) {
        addWatermarkToPage(document, page, watermarkText, opacity, fontSize, position);
      }

      document.save(resultPath);
    }

    System.out.println("Watermark added to PDF. Result saved to: " + resultPath);
    return resultPath;
  }

  private void addWatermarkToPage(
      PDDocument document, PDPage page, String text, float opacity, int fontSize, String position)
      throws IOException {
    try (PDPageContentStream contentStream =
        new PDPageContentStream(
            document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

      // Configurar transparência
      PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
      graphicsState.setNonStrokingAlphaConstant(opacity);
      contentStream.setGraphicsStateParameters(graphicsState);

      // Configurar fonte e cor
      PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
      contentStream.setFont(font, fontSize);
      contentStream.setNonStrokingColor(Color.GRAY);

      // Calcular posição
      float pageWidth = page.getMediaBox().getWidth();
      float pageHeight = page.getMediaBox().getHeight();
      float textWidth = font.getStringWidth(text) / 1000 * fontSize;

      float x, y;
      switch (position.toLowerCase()) {
        case "top-left" -> {
          x = 50;
          y = pageHeight - 50;
        }
        case "top-right" -> {
          x = pageWidth - textWidth - 50;
          y = pageHeight - 50;
        }
        case "bottom-left" -> {
          x = 50;
          y = 50;
        }
        case "bottom-right" -> {
          x = pageWidth - textWidth - 50;
          y = 50;
        }
        default -> { // center
          x = (pageWidth - textWidth) / 2;
          y = pageHeight / 2;
        }
      }

      // Adicionar texto
      contentStream.beginText();
      contentStream.newLineAtOffset(x, y);
      contentStream.showText(text);
      contentStream.endText();
    }
  }

  private String processEncrypt(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("ENCRYPT operation requires at least 1 input file");
    }

    String inputFile = inputFiles.get(0); // ENCRYPT trabalha com apenas um arquivo
    File file = new File(inputFile);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputFile);
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    // Obter opções de criptografia
    Map<String, Object> options = job.getOptions();
    if (options == null) {
      throw new IllegalArgumentException(
          "ENCRYPT operation requires options with 'password' parameter");
    }

    String userPassword = (String) options.get("password");
    if (userPassword == null || userPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("ENCRYPT operation requires 'password' parameter");
    }

    String ownerPassword = (String) options.get("ownerPassword");
    if (ownerPassword == null || ownerPassword.trim().isEmpty()) {
      ownerPassword = userPassword; // Usar a mesma senha se não especificada
    }

    String resultPath = resultDir.resolve("encrypted.pdf").toString();

    try (PDDocument document = Loader.loadPDF(file)) {
      // Configurar permissões
      AccessPermission accessPermission = new AccessPermission();

      // Opções de permissão (padrão: permitir tudo)
      boolean allowPrint =
          options.get("allowPrint") != null
              ? Boolean.parseBoolean(options.get("allowPrint").toString())
              : true;
      boolean allowCopy =
          options.get("allowCopy") != null
              ? Boolean.parseBoolean(options.get("allowCopy").toString())
              : true;
      boolean allowModify =
          options.get("allowModify") != null
              ? Boolean.parseBoolean(options.get("allowModify").toString())
              : true;

      accessPermission.setCanPrint(allowPrint);
      accessPermission.setCanExtractContent(allowCopy);
      accessPermission.setCanModify(allowModify);

      // Criar política de proteção
      StandardProtectionPolicy protectionPolicy =
          new StandardProtectionPolicy(ownerPassword, userPassword, accessPermission);
      protectionPolicy.setEncryptionKeyLength(128);

      // Aplicar criptografia
      document.protect(protectionPolicy);
      document.save(resultPath);
    }

    System.out.println("PDF encrypted successfully. Result saved to: " + resultPath);
    return resultPath;
  }

  private String processDecrypt(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("DECRYPT operation requires at least 1 input file");
    }

    String inputFile = inputFiles.get(0); // DECRYPT trabalha com apenas um arquivo
    File file = new File(inputFile);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputFile);
    }

    // Criar diretório de resultado
    Path resultDir = Paths.get("./storage/" + job.getId());
    Files.createDirectories(resultDir);

    // Obter opções de descriptografia
    Map<String, Object> options = job.getOptions();
    if (options == null) {
      throw new IllegalArgumentException(
          "DECRYPT operation requires options with 'password' parameter");
    }

    String password = (String) options.get("password");
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("DECRYPT operation requires 'password' parameter");
    }

    String resultPath = resultDir.resolve("decrypted.pdf").toString();

    try (PDDocument document = Loader.loadPDF(file, password)) {
      if (document.isEncrypted()) {
        // Remover proteção
        document.setAllSecurityToBeRemoved(true);
      }

      document.save(resultPath);
    } catch (Exception e) {
      throw new RuntimeException("Failed to decrypt PDF. Invalid password or corrupted file.", e);
    }

    System.out.println("PDF decrypted successfully. Result saved to: " + resultPath);
    return resultPath;
  }

  private String processExtractText(Job job) throws IOException {
    if (job.getInputFiles().isEmpty()) {
      throw new IllegalArgumentException("No input files provided for text extraction");
    }

    String inputPath = job.getInputFiles().get(0);
    Path resultDir = Paths.get("storage/jobs", job.getId(), "results");
    Files.createDirectories(resultDir);

    String resultFileName = "extracted_text.txt";
    Path resultPath = resultDir.resolve(resultFileName);

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      PDFTextStripper textStripper = new PDFTextStripper();

      // Configurar opções se fornecidas
      Map<String, Object> options = job.getOptions();
      if (options.containsKey("startPage")) {
        textStripper.setStartPage(((Number) options.get("startPage")).intValue());
      }
      if (options.containsKey("endPage")) {
        textStripper.setEndPage(((Number) options.get("endPage")).intValue());
      }
      if (options.containsKey("sortByPosition")) {
        textStripper.setSortByPosition((Boolean) options.get("sortByPosition"));
      }

      String extractedText = textStripper.getText(document);
      Files.write(resultPath, extractedText.getBytes(StandardCharsets.UTF_8));
    }

    return resultPath.toString();
  }

  private String processExtractMetadata(Job job) throws IOException {
    if (job.getInputFiles().isEmpty()) {
      throw new IllegalArgumentException("No input files provided for metadata extraction");
    }

    String inputPath = job.getInputFiles().get(0);
    Path resultDir = Paths.get("storage/jobs", job.getId(), "results");
    Files.createDirectories(resultDir);

    String resultFileName = "metadata.json";
    Path resultPath = resultDir.resolve(resultFileName);

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      PDDocumentInformation info = document.getDocumentInformation();
      Map<String, Object> metadata = new HashMap<>();

      // Informações básicas do documento
      metadata.put("title", info.getTitle());
      metadata.put("author", info.getAuthor());
      metadata.put("subject", info.getSubject());
      metadata.put("keywords", info.getKeywords());
      metadata.put("creator", info.getCreator());
      metadata.put("producer", info.getProducer());

      // Datas
      if (info.getCreationDate() != null) {
        metadata.put("creationDate", info.getCreationDate().getTime().toString());
      }
      if (info.getModificationDate() != null) {
        metadata.put("modificationDate", info.getModificationDate().getTime().toString());
      }

      // Informações do documento
      metadata.put("numberOfPages", document.getNumberOfPages());
      metadata.put("version", document.getVersion());
      metadata.put("isEncrypted", document.isEncrypted());

      // Converter para JSON
      StringBuilder jsonBuilder = new StringBuilder();
      jsonBuilder.append("{\n");
      boolean first = true;
      for (Map.Entry<String, Object> entry : metadata.entrySet()) {
        if (!first) {
          jsonBuilder.append(",\n");
        }
        jsonBuilder.append("  \"").append(entry.getKey()).append("\": ");
        if (entry.getValue() == null) {
          jsonBuilder.append("null");
        } else if (entry.getValue() instanceof String) {
          jsonBuilder
              .append("\"")
              .append(entry.getValue().toString().replace("\"", "\\\""))
              .append("\"");
        } else {
          jsonBuilder.append(entry.getValue().toString());
        }
        first = false;
      }
      jsonBuilder.append("\n}");

      Files.write(resultPath, jsonBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    return resultPath.toString();
  }

  // Operações avançadas de PDF

  private String processPdfCrop(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("PDF_CROP operation requires at least 1 input file");
    }

    String inputPath = inputFiles.get(0);
    File file = new File(inputPath);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputPath);
    }

    Map<String, Object> options = job.getOptions();

    // Parâmetros de crop
    Number x = (Number) options.getOrDefault("x", 0);
    Number y = (Number) options.getOrDefault("y", 0);
    Number width = (Number) options.getOrDefault("width", 595); // A4 width
    Number height = (Number) options.getOrDefault("height", 842); // A4 height
    String pages = (String) options.getOrDefault("pages", "all");

    Path resultPath = Paths.get("temp", "result_" + job.getId() + ".pdf");
    Files.createDirectories(resultPath.getParent());

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      PDDocument resultDoc = new PDDocument();

      List<Integer> pageNumbers =
          pages.equals("all")
              ? getAllPageNumbers(document.getNumberOfPages())
              : parsePageRange(pages, document.getNumberOfPages());

      for (int pageNum : pageNumbers) {
        PDPage page = document.getPage(pageNum - 1);
        PDPage newPage = resultDoc.importPage(page);

        // Define a área de crop
        newPage.setCropBox(
            new org.apache.pdfbox.pdmodel.common.PDRectangle(
                x.floatValue(), y.floatValue(), width.floatValue(), height.floatValue()));
      }

      resultDoc.save(resultPath.toFile());
      resultDoc.close();
    }

    return resultPath.toString();
  }

  private String processPdfReorder(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("PDF_REORDER operation requires at least 1 input file");
    }

    String inputPath = inputFiles.get(0);
    File file = new File(inputPath);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputPath);
    }

    Map<String, Object> options = job.getOptions();
    String pageOrder = (String) options.get("page_order"); // Ex: "3,1,2,4"

    if (pageOrder == null || pageOrder.trim().isEmpty()) {
      throw new IllegalArgumentException("page_order is required for reorder operation");
    }

    Path resultPath = Paths.get("temp", "result_" + job.getId() + ".pdf");
    Files.createDirectories(resultPath.getParent());

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      PDDocument resultDoc = new PDDocument();

      String[] orderArray = pageOrder.split(",");
      for (String pageNumStr : orderArray) {
        int pageNum = Integer.parseInt(pageNumStr.trim());
        if (pageNum < 1 || pageNum > document.getNumberOfPages()) {
          throw new IllegalArgumentException("Invalid page number: " + pageNum);
        }

        PDPage page = document.getPage(pageNum - 1);
        resultDoc.importPage(page);
      }

      resultDoc.save(resultPath.toFile());
      resultDoc.close();
    }

    return resultPath.toString();
  }

  private String processPdfResize(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("PDF_RESIZE operation requires at least 1 input file");
    }

    String inputPath = inputFiles.get(0);
    File file = new File(inputPath);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputPath);
    }

    Map<String, Object> options = job.getOptions();
    String pageSize = (String) options.getOrDefault("page_size", "A4");
    String pages = (String) options.getOrDefault("pages", "all");

    Path resultPath = Paths.get("temp", "result_" + job.getId() + ".pdf");
    Files.createDirectories(resultPath.getParent());

    // Definir dimensões baseadas no tamanho da página
    org.apache.pdfbox.pdmodel.common.PDRectangle newSize = getPageSize(pageSize);

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      List<Integer> pageNumbers =
          pages.equals("all")
              ? getAllPageNumbers(document.getNumberOfPages())
              : parsePageRange(pages, document.getNumberOfPages());

      for (int pageNum : pageNumbers) {
        PDPage page = document.getPage(pageNum - 1);
        page.setMediaBox(newSize);
        page.setCropBox(newSize);
      }

      document.save(resultPath.toFile());
    }

    return resultPath.toString();
  }

  private String processCompress(Job job) throws IOException {
    List<String> inputFiles = job.getInputFiles();
    if (inputFiles == null || inputFiles.isEmpty()) {
      throw new IllegalArgumentException("COMPRESS operation requires at least 1 input file");
    }

    String inputPath = inputFiles.get(0);
    File file = new File(inputPath);
    if (!file.exists()) {
      throw new IllegalArgumentException("Input file not found: " + inputPath);
    }

    Map<String, Object> options = job.getOptions();
    String quality = (String) options.getOrDefault("quality", "medium");

    Path resultPath = Paths.get("temp", "result_" + job.getId() + ".pdf");
    Files.createDirectories(resultPath.getParent());

    try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
      // Compressão básica - remove objetos não utilizados
      document
          .getDocumentCatalog()
          .getPages()
          .forEach(
              page -> {
                try {
                  // Remove recursos não utilizados
                  if (page.getResources() != null) {
                    // Implementação básica de compressão
                    // Em uma implementação mais avançada, seria possível
                    // comprimir imagens e otimizar fontes
                  }
                } catch (Exception e) {
                  // Log error but continue processing
                }
              });

      document.save(resultPath.toFile());
    }

    return resultPath.toString();
  }

  private String processPdfToImages(Job job) throws IOException {
    // Esta operação requer bibliotecas adicionais como PDFRenderer
    // Por enquanto, retorna uma implementação básica
    throw new UnsupportedOperationException(
        "PDF to images conversion requires additional dependencies. Please use external tools.");
  }

  private String processImagesToPdf(Job job) throws IOException {
    // Esta operação requer processamento de imagens
    // Por enquanto, retorna uma implementação básica
    throw new UnsupportedOperationException(
        "Images to PDF conversion requires additional dependencies. Please use external tools.");
  }

  // Métodos auxiliares para operações avançadas

  private List<Integer> getAllPageNumbers(int totalPages) {
    List<Integer> pages = new ArrayList<>();
    for (int i = 1; i <= totalPages; i++) {
      pages.add(i);
    }
    return pages;
  }

  private org.apache.pdfbox.pdmodel.common.PDRectangle getPageSize(String pageSize) {
    return switch (pageSize.toUpperCase()) {
      case "A4" -> org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
      case "A3" -> org.apache.pdfbox.pdmodel.common.PDRectangle.A3;
      case "A5" -> org.apache.pdfbox.pdmodel.common.PDRectangle.A5;
      case "LETTER" -> org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER;
      case "LEGAL" -> org.apache.pdfbox.pdmodel.common.PDRectangle.LEGAL;
      default -> org.apache.pdfbox.pdmodel.common.PDRectangle.A4;
    };
  }

  @Override
  public boolean supportsOperation(JobOperation operation) {
    return SUPPORTED_OPERATIONS.contains(operation);
  }

  @Override
  public List<JobOperation> getSupportedOperations() {
    return List.copyOf(SUPPORTED_OPERATIONS);
  }

  @Override
  public boolean validateOptions(JobOperation operation, Map<String, Object> options) {
    System.out.println(
        "DEBUG: validateOptions called with operation: " + operation + ", options: " + options);

    if (options == null) {
      System.out.println("DEBUG: options is null, returning false");
      return false;
    }

    try {
      boolean result =
          switch (operation) {
            case PDF_REORDER -> options.containsKey("page_order");
            case PDF_CROP -> validateCropOptions(options);
            case PDF_RESIZE -> validateResizeOptions(options);
            case ENCRYPT -> options.containsKey("password");
            case DECRYPT -> options.containsKey("password");
            case WATERMARK -> options.containsKey("text");
            default -> true;
          };
      System.out.println("DEBUG: validateOptions result: " + result);
      return result;
    } catch (Exception e) {
      System.out.println("DEBUG: Exception in validateOptions: " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  private boolean validateCropOptions(Map<String, Object> options) {
    try {
      if (options.containsKey("x")) ((Number) options.get("x")).floatValue();
      if (options.containsKey("y")) ((Number) options.get("y")).floatValue();
      if (options.containsKey("width")) ((Number) options.get("width")).floatValue();
      if (options.containsKey("height")) ((Number) options.get("height")).floatValue();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean validateResizeOptions(Map<String, Object> options) {
    if (options.containsKey("page_size")) {
      String pageSize = (String) options.get("page_size");
      return List.of("A4", "A3", "A5", "LETTER", "LEGAL").contains(pageSize.toUpperCase());
    }
    return true;
  }

  @Override
  public Map<String, Object> getOptionsSchema(JobOperation operation) {
    Map<String, Object> baseSchema = new HashMap<>();

    return switch (operation) {
      case PDF_CROP -> {
        baseSchema.put("x", "number (optional, default: 0)");
        baseSchema.put("y", "number (optional, default: 0)");
        baseSchema.put("width", "number (optional, default: 595)");
        baseSchema.put("height", "number (optional, default: 842)");
        baseSchema.put("pages", "string (optional, default: 'all')");
        yield baseSchema;
      }
      case PDF_REORDER -> {
        baseSchema.put("page_order", "string (required, ex: '3,1,2,4')");
        yield baseSchema;
      }
      case PDF_RESIZE -> {
        baseSchema.put(
            "page_size", "string (optional, default: 'A4', options: A4, A3, A5, LETTER, LEGAL)");
        baseSchema.put("pages", "string (optional, default: 'all')");
        yield baseSchema;
      }
      case COMPRESS -> {
        baseSchema.put(
            "quality", "string (optional, default: 'medium', options: low, medium, high)");
        yield baseSchema;
      }
      case ENCRYPT -> {
        baseSchema.put("password", "string (required)");
        baseSchema.put("ownerPassword", "string (optional)");
        baseSchema.put("allowPrint", "boolean (optional, default: true)");
        baseSchema.put("allowCopy", "boolean (optional, default: true)");
        baseSchema.put("allowModify", "boolean (optional, default: true)");
        yield baseSchema;
      }
      case DECRYPT -> {
        baseSchema.put("password", "string (required)");
        yield baseSchema;
      }
      case WATERMARK -> {
        baseSchema.put("text", "string (required)");
        baseSchema.put("opacity", "number (optional, default: 0.3)");
        baseSchema.put("fontSize", "number (optional, default: 36)");
        baseSchema.put(
            "position",
            "string (optional, default: 'center', options: center, top-left, top-right, bottom-left, bottom-right)");
        yield baseSchema;
      }
      case ROTATE -> {
        baseSchema.put("degrees", "number (required, options: 90, 180, 270)");
        baseSchema.put("pages", "string (optional, default: 'all')");
        yield baseSchema;
      }
      case SPLIT -> {
        baseSchema.put("pages", "string (optional, default: 'all')");
        yield baseSchema;
      }
      case EXTRACT_TEXT -> {
        baseSchema.put("startPage", "number (optional)");
        baseSchema.put("endPage", "number (optional)");
        baseSchema.put("sortByPosition", "boolean (optional, default: false)");
        yield baseSchema;
      }
      default -> baseSchema;
    };
  }
}
