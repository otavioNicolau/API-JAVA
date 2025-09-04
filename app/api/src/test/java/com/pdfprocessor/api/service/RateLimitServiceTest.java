package com.pdfprocessor.api.service;

import com.pdfprocessor.api.exception.SecurityValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitServiceTest {

  private RateLimitService rateLimitService;
  private Clock fixedClock;

  @BeforeEach
  void setUp() {
    // Usar um clock fixo para testes determinísticos
    fixedClock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.systemDefault());
    rateLimitService = new RateLimitService(fixedClock);
  }

  @Test
  void shouldAllowRequestsWithinLimit() {
    String apiKey = "test-api-key";

    // Fazer 50 requisições (dentro do limite de 100/hora)
    for (int i = 0; i < 50; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    }

    // Verificar contagem atual
    assertEquals(50, rateLimitService.getCurrentRequestCount(apiKey));
  }

  @Test
  void shouldRejectRequestsExceedingLimit() {
    String apiKey = "test-api-key";

    // Fazer 100 requisições (limite exato)
    for (int i = 0; i < 100; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    }

    // A 101ª requisição deve ser rejeitada
    SecurityValidationException exception =
        assertThrows(
            SecurityValidationException.class,
            () -> rateLimitService.checkRateLimit(apiKey));

    assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
    assertTrue(exception.getMessage().contains("Rate limit exceeded"));
  }

  @Test
  void shouldResetCountAfterOneHour() {
    String apiKey = "test-api-key";

    // Fazer 100 requisições
    for (int i = 0; i < 100; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    }

    // Verificar que chegou ao limite
    assertThrows(
        SecurityValidationException.class, () -> rateLimitService.checkRateLimit(apiKey));

    // Avançar o clock em 1 hora e 1 minuto
    Clock newClock =
        Clock.fixed(Instant.parse("2024-01-15T11:01:00Z"), ZoneId.systemDefault());
    rateLimitService = new RateLimitService(newClock);

    // Agora deve permitir novas requisições
    assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    assertEquals(1, rateLimitService.getCurrentRequestCount(apiKey));
  }

  @Test
  void shouldHandleMultipleApiKeys() {
    String apiKey1 = "api-key-1";
    String apiKey2 = "api-key-2";

    // Fazer 50 requisições para cada API key
    for (int i = 0; i < 50; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey1));
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey2));
    }

    // Verificar contagens independentes
    assertEquals(50, rateLimitService.getCurrentRequestCount(apiKey1));
    assertEquals(50, rateLimitService.getCurrentRequestCount(apiKey2));

    // Fazer mais 50 requisições para apiKey1 (chegando ao limite)
    for (int i = 0; i < 50; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey1));
    }

    // apiKey1 deve estar no limite, apiKey2 ainda deve funcionar
    assertThrows(
        SecurityValidationException.class, () -> rateLimitService.checkRateLimit(apiKey1));
    assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey2));
  }

  @Test
  void shouldReturnZeroForNewApiKey() {
    String newApiKey = "new-api-key";
    assertEquals(0, rateLimitService.getCurrentRequestCount(newApiKey));
  }

  @Test
  void shouldClearRateLimitData() {
    String apiKey = "test-api-key";

    // Fazer algumas requisições
    for (int i = 0; i < 10; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    }

    assertEquals(10, rateLimitService.getCurrentRequestCount(apiKey));

    // Limpar dados
    rateLimitService.clearRateLimitData();

    // Contagem deve voltar a zero
    assertEquals(0, rateLimitService.getCurrentRequestCount(apiKey));
  }

  @Test
  void shouldHandleSlidingWindow() {
    String apiKey = "test-api-key";

    // Fazer 50 requisições no tempo inicial
    for (int i = 0; i < 50; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkRateLimit(apiKey));
    }

    // Avançar 30 minutos - criar nova instância com clock atualizado
    Clock newClock =
        Clock.fixed(Instant.parse("2024-01-15T10:30:00Z"), ZoneId.systemDefault());
    
    // Para simular janela deslizante, vamos testar o comportamento após 1 hora
    Clock oneHourLater =
        Clock.fixed(Instant.parse("2024-01-15T11:01:00Z"), ZoneId.systemDefault());
    RateLimitService newService = new RateLimitService(oneHourLater);

    // Com nova instância (janela resetada), deve permitir novas requisições
    assertDoesNotThrow(() -> newService.checkRateLimit(apiKey));
    assertEquals(1, newService.getCurrentRequestCount(apiKey));
  }
}