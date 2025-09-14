package com.pdfprocessor.infrastructure.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/** Testes unitários para RedisConfig. */
class RedisConfigTest {

  private RedisConfig redisConfig;
  private RedisConnectionFactory connectionFactory;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    redisConfig = new RedisConfig();
    connectionFactory = mock(RedisConnectionFactory.class);
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldCreateRedisTemplateWithCorrectConfiguration() {
    // When
    RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(connectionFactory);

    // Then
    assertNotNull(redisTemplate);
    assertEquals(connectionFactory, redisTemplate.getConnectionFactory());

    // Verificar serializadores
    assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
    assertTrue(redisTemplate.getHashKeySerializer() instanceof StringRedisSerializer);
    assertTrue(redisTemplate.getValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
    assertTrue(
        redisTemplate.getHashValueSerializer() instanceof GenericJackson2JsonRedisSerializer);
  }

  @Test
  void shouldCreateRedisConnectionFactory() {
    // When
    RedisConnectionFactory factory = redisConfig.redisConnectionFactory();

    // Then
    assertNotNull(factory);
    assertTrue(
        factory
            instanceof org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory);
  }

  @Test
  void shouldReturnDifferentRedisTemplateInstances() {
    // When
    RedisTemplate<String, Object> template1 = redisConfig.redisTemplate(connectionFactory);
    RedisTemplate<String, Object> template2 = redisConfig.redisTemplate(connectionFactory);

    // Then
    assertNotNull(template1);
    assertNotNull(template2);
    assertNotSame(template1, template2); // Diferentes instâncias
  }

  @Test
  void shouldReturnDifferentRedisConnectionFactoryInstances() {
    // When
    RedisConnectionFactory factory1 = redisConfig.redisConnectionFactory();
    RedisConnectionFactory factory2 = redisConfig.redisConnectionFactory();

    // Then
    assertNotNull(factory1);
    assertNotNull(factory2);
    assertNotSame(factory1, factory2); // Diferentes instâncias
  }

  @Test
  void shouldHandleNullConnectionFactory() {
    // Given
    RedisConfig config = new RedisConfig();

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          config.redisTemplate(null);
        });
  }

  @Test
  void shouldConfigureRedisTemplateAfterPropertiesSet() {
    // When
    RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(connectionFactory);

    // Then
    assertNotNull(redisTemplate);
    // Verificar se o template foi inicializado corretamente
    assertNotNull(redisTemplate.getConnectionFactory());
  }

  @Test
  void shouldConfigureRedisTemplateWithDefaultSerializer() {
    // When
    RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(connectionFactory);

    // Then
    assertNotNull(redisTemplate);
    assertNotNull(redisTemplate.getDefaultSerializer());
  }
}
