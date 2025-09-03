package com.pdfprocessor.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuração web da aplicação. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired private LoggingInterceptor loggingInterceptor;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(loggingInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/v1/monitoring/health"); // Evitar logs excessivos do health check
  }
}
