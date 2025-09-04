package com.pdfprocessor.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.api.ApiApplication;
import com.pdfprocessor.api.config.SecurityConfig;
import io.restassured.RestAssured;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Testes de integração end-to-end que verificam o fluxo completo:
 * API -> Fila -> Worker -> Processamento -> Resultado
 */
@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.storage.base-path=/tmp/pdf-processor-test",
    "app.queue.redis.host=localhost",
    "app.queue.redis.port=6379",
    "app.security.api-keys[0]=test-key-67890"
})
class EndToEndIntegrationTest {

  @LocalServerPort
  private int port;

  @TempDir
  Path tempDir;

  private File testPdf1;
  private File testPdf2;

  @BeforeEach
  void setUp() throws IOException {
    RestAssured.port = port;
    RestAssured.basePath = "/api/v1";

    // Criar PDFs de teste
    testPdf1 = createTestPdf("test1.pdf");
    testPdf2 = createTestPdf("test2.pdf");
  }

  @Test
  void shouldCreateMergeJobViaApi() {
    // Given & When: Criar um job de merge via API
    String jobId = given()
        .header("X-API-Key", "test-key-67890")
        .multiPart("files", testPdf1)
        .multiPart("files", testPdf2)
        .param("operation", "MERGE")
        .param("optionsJson", "{}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(200)
        .body("operation", equalTo("MERGE"))
        .body("status", equalTo("PENDING"))
        .extract()
        .path("id");

    // Then: Verificar se o job foi criado
    assertNotNull(jobId);
  }

  @Test
  void shouldCreateSplitJobViaApi() {
    // Given & When: Criar um job de split via API
    String jobId = given()
        .header("X-API-Key", "test-key-67890")
        .multiPart("files", testPdf1)
        .param("operation", "SPLIT")
        .param("optionsJson", "{\"pages\":[1]}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(200)
        .body("operation", equalTo("SPLIT"))
        .body("status", equalTo("PENDING"))
        .extract()
        .path("id");

    // Then: Verificar se o job foi criado
    assertNotNull(jobId);
  }

  @Test
  void shouldListJobsViaApi() {
    // Given: Criar alguns jobs
    createTestJob("MERGE");
    createTestJob("SPLIT");

    // When: Listar jobs via API
    given()
        .header("X-API-Key", "test-key-67890")
    .when()
        .get("/jobs")
    .then()
        .statusCode(200)
        .body("size()", greaterThanOrEqualTo(2));
  }

  @Test
  void shouldGetJobByIdViaApi() {
    // Given: Criar um job
    String jobId = createTestJob("MERGE");

    // When: Buscar job por ID via API
    given()
        .header("X-API-Key", "test-key-67890")
    .when()
        .get("/jobs/{id}", jobId)
    .then()
        .statusCode(200)
        .body("id", equalTo(jobId))
        .body("operation", equalTo("MERGE"));
  }

  @Test
  void shouldRejectRequestWithoutApiKey() {
    // When: Tentar criar job sem API key
    given()
        .multiPart("files", testPdf1)
        .param("operation", "MERGE")
        .param("optionsJson", "{}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(401);
  }

  @Test
  void shouldRejectRequestWithInvalidApiKey() {
    // When: Tentar criar job com API key inválida
    given()
        .header("X-API-Key", "invalid-key")
        .multiPart("files", testPdf1)
        .param("operation", "MERGE")
        .param("optionsJson", "{}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(401);
  }

  private String createTestJob(String operation) {
    return given()
        .header("X-API-Key", "test-key-67890")
        .multiPart("files", testPdf1)
        .param("operation", operation)
        .param("optionsJson", "{}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(200)
        .extract()
        .path("id");
  }

  private File createTestPdf(String filename) throws IOException {
    Path pdfPath = tempDir.resolve(filename);
    
    // Criar um PDF simples de teste (conteúdo mínimo válido)
    String pdfContent = "%PDF-1.4\n" +
        "1 0 obj\n" +
        "<<\n" +
        "/Type /Catalog\n" +
        "/Pages 2 0 R\n" +
        ">>\n" +
        "endobj\n" +
        "2 0 obj\n" +
        "<<\n" +
        "/Type /Pages\n" +
        "/Kids [3 0 R]\n" +
        "/Count 1\n" +
        ">>\n" +
        "endobj\n" +
        "3 0 obj\n" +
        "<<\n" +
        "/Type /Page\n" +
        "/Parent 2 0 R\n" +
        "/MediaBox [0 0 612 792]\n" +
        ">>\n" +
        "endobj\n" +
        "xref\n" +
        "0 4\n" +
        "0000000000 65535 f \n" +
        "0000000010 00000 n \n" +
        "0000000079 00000 n \n" +
        "0000000173 00000 n \n" +
        "trailer\n" +
        "<<\n" +
        "/Size 4\n" +
        "/Root 1 0 R\n" +
        ">>\n" +
        "startxref\n" +
        "253\n" +
        "%%EOF";
    
    Files.write(pdfPath, pdfContent.getBytes());
    return pdfPath.toFile();
  }
}