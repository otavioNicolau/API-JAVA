package com.pdfprocessor.api.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filtro para adicionar headers de segurança HTTP.
 * Implementa proteções contra ataques comuns como XSS, clickjacking, MIME sniffing, etc.
 */
@Component
@Order(1)
public class SecurityHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    
    // Previne MIME sniffing attacks
    httpResponse.setHeader("X-Content-Type-Options", "nosniff");
    
    // Previne clickjacking attacks
    httpResponse.setHeader("X-Frame-Options", "DENY");
    
    // Habilita proteção XSS no browser
    httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
    
    // Content Security Policy - permite recursos do Swagger UI
    String requestURI = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
    if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/webjars")) {
      httpResponse.setHeader("Content-Security-Policy", 
          "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self';");
    } else {
      httpResponse.setHeader("Content-Security-Policy", 
          "default-src 'none'; script-src 'none'; object-src 'none'; base-uri 'none';");
    }
    
    // Referrer Policy - controla informações de referrer
    httpResponse.setHeader("Referrer-Policy", "no-referrer");
    
    // Permissions Policy - desabilita recursos desnecessários
    httpResponse.setHeader("Permissions-Policy", 
        "camera=(), microphone=(), geolocation=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()");
    
    // Strict Transport Security - força HTTPS (apenas se estiver em HTTPS)
    String scheme = request.getScheme();
    if ("https".equals(scheme)) {
      httpResponse.setHeader("Strict-Transport-Security", 
          "max-age=31536000; includeSubDomains; preload");
    }
    
    // Cache Control para endpoints sensíveis
    if (requestURI.contains("/api/")) {
      httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      httpResponse.setHeader("Pragma", "no-cache");
      httpResponse.setHeader("Expires", "0");
    }
    
    // Server header - remove informações do servidor
    httpResponse.setHeader("Server", "PDF-Processor-API");
    
    chain.doFilter(request, response);
  }
}