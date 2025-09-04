package com.pdfprocessor.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.Collections;

/**
 * Filtro de autenticação via X-API-Key header. Valida se o header X-API-Key contém uma chave
 * válida.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
  private static final String API_KEY_HEADER = "X-API-Key";
  private final Set<String> validApiKeys;

  public ApiKeyAuthenticationFilter(String[] validApiKeys) {
    System.out.println("[DEBUG] ===== ApiKeyAuthenticationFilter CONSTRUTOR CHAMADO =====");
    System.out.println("[DEBUG] Chaves API válidas: " + Arrays.toString(validApiKeys));
    this.validApiKeys = Set.of(validApiKeys);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    System.out.println("[FILTER] ApiKeyAuthenticationFilter executado para: " + request.getRequestURI());
    LOGGER.info("[FILTER] ApiKeyAuthenticationFilter executado para: {}", request.getRequestURI());
    System.out.println("[DEBUG] ===== ApiKeyAuthenticationFilter EXECUTADO =====");
    System.out.println("[DEBUG] Request URI: " + request.getRequestURI());
    System.out.println("[DEBUG] Request Method: " + request.getMethod());
    
    String requestURI = request.getRequestURI();
    if (isPublicEndpoint(requestURI)) {
      System.out.println("[DEBUG] Endpoint público, permitindo acesso");
      filterChain.doFilter(request, response);
      return;
    }

    String apiKey = request.getHeader("X-API-Key");
    System.out.println("[DEBUG] Chave API recebida: " + apiKey);
    
    if (apiKey == null || !isValidApiKey(apiKey)) {
      System.out.println("[DEBUG] Chave API ausente ou inválida, retornando 401");
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid API key required\"}");
      return;
    }

    System.out.println("[DEBUG] Chave API válida, definindo autenticação no SecurityContext");
    
    try {
      // Define a autenticação no SecurityContext
      System.out.println("[DEBUG] Criando UsernamePasswordAuthenticationToken...");
      UsernamePasswordAuthenticationToken authentication = 
          new UsernamePasswordAuthenticationToken(
              "api-user", 
              null, 
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER"))
          );
      System.out.println("[DEBUG] Token criado: " + authentication);
      System.out.println("[DEBUG] Authorities: " + authentication.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
      System.out.println("[DEBUG] Autenticação definida com sucesso!");
      System.out.println("[DEBUG] SecurityContext após definir: " + SecurityContextHolder.getContext().getAuthentication());
    } catch (Exception e) {
      System.out.println("[DEBUG] ERRO ao definir autenticação: " + e.getMessage());
      e.printStackTrace();
    }
    
    System.out.println("[DEBUG] Continuando com a requisição...");
    filterChain.doFilter(request, response);
    System.out.println("[DEBUG] Requisição processada, retornando do filtro");
  }

  private boolean isValidApiKey(String apiKey) {
    return validApiKeys.contains(apiKey);
  }

  private boolean isPublicEndpoint(String path) {
    return path.startsWith("/actuator/health")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs")
        || path.equals("/swagger-ui.html")
        || path.equals("/api/v1/monitoring/health")
        || path.equals("/favicon.ico")
        || path.startsWith("/webjars/")
        || path.startsWith("/swagger-resources/")
        || path.startsWith("/@vite/")
        || path.endsWith(".css")
        || path.endsWith(".js")
        || path.endsWith(".png")
        || path.endsWith(".ico");
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
