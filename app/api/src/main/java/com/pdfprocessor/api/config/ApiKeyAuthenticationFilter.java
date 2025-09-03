package com.pdfprocessor.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro de autenticação via X-API-Key header. Valida se o header X-API-Key contém uma chave
 * válida.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
  private static final String API_KEY_HEADER = "X-API-Key";
  private final List<String> validApiKeys;

  public ApiKeyAuthenticationFilter(String[] validApiKeys) {
    this.validApiKeys = Arrays.asList(validApiKeys);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestPath = request.getRequestURI();
    LOGGER.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());

    // Permitir endpoints públicos
    if (isPublicEndpoint(requestPath)) {
      LOGGER.info("Public endpoint, allowing access: {}", requestPath);
      filterChain.doFilter(request, response);
      return;
    }

    // Verificar se é endpoint da API que requer autenticação
    if (requestPath.startsWith("/api/v1/")) {
      String apiKey = request.getHeader(API_KEY_HEADER);
      LOGGER.info(
          "API endpoint detected: {}, X-API-Key: {}",
          requestPath,
          apiKey != null ? "[PRESENT]" : "[MISSING]");

      if (apiKey == null || apiKey.trim().isEmpty()) {
        LOGGER.warn("Missing X-API-Key header for endpoint: {}", requestPath);
        sendUnauthorizedResponse(response, "Missing X-API-Key header");
        return;
      }

      if (!validApiKeys.contains(apiKey)) {
        LOGGER.warn("Invalid X-API-Key for endpoint: {}", requestPath);
        sendUnauthorizedResponse(response, "Invalid X-API-Key");
        return;
      }

      LOGGER.info("Valid X-API-Key, allowing access to: {}", requestPath);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/actuator/health")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.equals("/swagger-ui.html")
        || path.equals("/api/v1/monitoring/health");
  }

  private void sendUnauthorizedResponse(HttpServletResponse response, String message)
      throws IOException {
    LOGGER.info("Sending unauthorized response: {}", message);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType("application/json");
    response
        .getWriter()
        .write(
            String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message));
    response.getWriter().flush();
  }
}
