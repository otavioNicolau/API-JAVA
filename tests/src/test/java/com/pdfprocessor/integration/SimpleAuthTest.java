package com.pdfprocessor.integration;

import static io.restassured.RestAssured.given;

import com.pdfprocessor.api.ApiApplication;
import com.pdfprocessor.api.config.SecurityConfig;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.storage.base-path=/tmp/pdf-processor-test",
    "app.queue.redis.host=localhost",
    "app.queue.redis.port=6379",
    "app.security.api-keys[0]=test-key-67890"
})
class SimpleAuthTest {

  @LocalServerPort
  private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    RestAssured.basePath = "/api/v1";
  }

  @Test
  void shouldRejectGetRequestWithoutApiKey() {
    System.out.println("Testing GET request without API key");
    given()
    .when()
        .get("/jobs")
    .then()
        .statusCode(401);
  }

  @Test
  void shouldRejectPostRequestWithoutApiKey() {
    System.out.println("Testing POST request without API key");
    given()
        .contentType("application/json")
        .body("{}")
    .when()
        .post("/jobs")
    .then()
        .statusCode(401);
  }

  @Test
  void shouldAcceptGetRequestWithValidApiKey() {
    System.out.println("Testing GET request with valid API key");
    System.out.println("RestAssured port: " + RestAssured.port);
    System.out.println("RestAssured basePath: " + RestAssured.basePath);
    System.out.println("Full URL: http://localhost:" + RestAssured.port + RestAssured.basePath + "/jobs");
    
    var response = given()
        .header("X-API-Key", "test-key-67890")
        .log().all()
    .when()
        .get("/jobs")
    .then()
        .log().all()
        .extract().response();
        
    System.out.println("Response status: " + response.getStatusCode());
    System.out.println("Response body: " + response.getBody().asString());
    System.out.println("Response headers: " + response.getHeaders());
    
    response.then().statusCode(200);
  }
}