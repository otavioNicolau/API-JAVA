package com.pdfprocessor.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuração do OpenAPI/Swagger para documentação da API. */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("PDF Processor API")
                .version("1.0.0")
                .description("API para processamento assíncrono de arquivos PDF")
                .contact(new Contact().name("PDF Processor Team").email("support@pdfprocessor.com"))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Servidor de desenvolvimento"),
                new Server()
                    .url("https://api.pdfprocessor.com")
                    .description("Servidor de produção")))
        .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "ApiKeyAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description(
                            "Chave de API para autenticação. Use uma das chaves válidas: dev-key-12345, test-key-67890, prod-key-abcdef")));
  }
}
