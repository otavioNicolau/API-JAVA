package com.pdfprocessor.domain.model;

/** Operações disponíveis para processamento de PDF. */
public enum JobOperation {
  // Operações básicas
  MERGE("Combinar múltiplos PDFs em um único arquivo"),
  SPLIT("Dividir um PDF em múltiplos arquivos"),
  COMPRESS("Comprimir PDF reduzindo tamanho"),
  PDF_TO_IMAGES("Converter páginas do PDF em imagens"),
  IMAGES_TO_PDF("Criar PDF a partir de imagens"),
  ROTATE("Rotacionar páginas do PDF"),
  WATERMARK("Adicionar marca d'água ao PDF"),
  ENCRYPT("Criptografar PDF com senha"),
  DECRYPT("Descriptografar PDF"),
  EXTRACT_METADATA("Extrair metadados do PDF"),
  EXTRACT_TEXT("Extrair texto do PDF"),

  // Operações avançadas
  PDF_CROP("Recortar páginas do PDF"),
  PDF_REORDER("Reordenar páginas do PDF"),
  PDF_RESIZE("Redimensionar páginas do PDF"),
  PDF_COMPARE("Comparar dois PDFs"),
  PDF_CREATE("Criar PDF a partir de texto/HTML/imagens"),
  PDF_EDIT("Editar PDF com anotações"),
  PDF_PROTECT("Proteger PDF com permissões"),
  PDF_UNLOCK("Remover proteções do PDF"),
  PDF_OPTIMIZE("Otimizar PDF para tamanho/performance"),
  PDF_VALIDATE("Validar conformidade do PDF"),
  PDF_REPAIR("Reparar PDF corrompido"),
  PDF_EXTRACT_RESOURCES("Extrair recursos do PDF"),
  PDF_REMOVE_RESOURCES("Remover recursos do PDF"),
  PDF_TO_PDFA("Converter para PDF/A"),
  PDF_OCR("Aplicar OCR ao PDF"),
  PDF_TO_AUDIO("Converter texto do PDF em áudio"),
  PDF_FROM_EPUB("Converter EPUB para PDF"),
  PDF_FROM_DJVU("Converter DjVu para PDF"),
  PDF_SIGN("Assinar PDF digitalmente");

  private final String description;

  JobOperation(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
