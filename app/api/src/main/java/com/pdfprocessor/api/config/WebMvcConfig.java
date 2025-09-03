package com.pdfprocessor.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração do Spring MVC para garantir que os controllers tenham prioridade sobre recursos
 * estáticos.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Configurar recursos estáticos apenas para caminhos específicos
    // Isso evita conflitos com os endpoints da API
    registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

    registry
        .addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }
}
