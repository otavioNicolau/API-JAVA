package com.pdfprocessor.integration;

import com.pdfprocessor.api.ApiApplication;
import com.pdfprocessor.api.config.ApiKeyAuthenticationFilter;
import com.pdfprocessor.api.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.storage.base-path=/tmp/pdf-processor-test",
    "app.queue.redis.host=localhost",
    "app.queue.redis.port=6379",
    "app.security.api-keys[0]=test-key-67890"
})
class SecurityConfigTest {

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  void shouldLoadSecurityConfig() {
    System.out.println("[DEBUG] Verificando se SecurityConfig está carregado...");
    assertTrue(applicationContext.containsBean("securityConfig"));
    
    SecurityConfig securityConfig = applicationContext.getBean(SecurityConfig.class);
    assertNotNull(securityConfig);
    System.out.println("[DEBUG] SecurityConfig carregado: " + securityConfig);
  }

  @Test
  void shouldLoadApiKeyAuthenticationFilter() {
    System.out.println("[DEBUG] Verificando se ApiKeyAuthenticationFilter está carregado...");
    assertTrue(applicationContext.containsBean("apiKeyAuthenticationFilter"));
    
    ApiKeyAuthenticationFilter filter = applicationContext.getBean(ApiKeyAuthenticationFilter.class);
    assertNotNull(filter);
    System.out.println("[DEBUG] ApiKeyAuthenticationFilter carregado: " + filter);
  }

  @Test
  void shouldHaveCorrectApiKeys() {
    SecurityConfig securityConfig = applicationContext.getBean(SecurityConfig.class);
    assertNotNull(securityConfig.getApiKeys());
    assertTrue(securityConfig.getApiKeys().contains("test-key-67890"));
    System.out.println("[DEBUG] Chaves API configuradas: " + securityConfig.getApiKeys());
  }
}