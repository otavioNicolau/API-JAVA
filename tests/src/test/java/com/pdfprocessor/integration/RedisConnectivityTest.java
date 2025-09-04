package com.pdfprocessor.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = com.pdfprocessor.api.ApiApplication.class)
@ActiveProfiles("test")
class RedisConnectivityTest {

  @Autowired private RedisTemplate<String, Object> redisTemplate;

  @Test
  void shouldConnectToRedis() {
    // Test basic Redis connectivity
    String testKey = "test:connectivity";
    String testValue = "test-value";
    
    // Set a value
    redisTemplate.opsForValue().set(testKey, testValue);
    
    // Get the value back
    String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
    
    assertEquals(testValue, retrievedValue);
    
    // Clean up
    redisTemplate.delete(testKey);
    
    System.out.println("Redis connectivity test passed!");
  }
}