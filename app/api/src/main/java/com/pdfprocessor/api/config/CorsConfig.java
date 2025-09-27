package com.pdfprocessor.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuração CORS para permitir requisições do Swagger UI e outras origens.
 */
@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Permitir todas as origens para desenvolvimento
    configuration.addAllowedOriginPattern("*");
    
    // Permitir todos os métodos HTTP
    configuration.addAllowedMethod("*");
    
    // Permitir todos os headers, incluindo X-API-Key
    configuration.addAllowedHeader("*");
    
    // Permitir credenciais
    configuration.setAllowCredentials(true);
    
    // Expor headers de resposta
    configuration.addExposedHeader("*");
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    
    return source;
  }
}
