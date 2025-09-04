package com.pdfprocessor.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private SecurityHeadersFilter securityHeadersFilter;

  @BeforeEach
  void setUp() {
    securityHeadersFilter = new SecurityHeadersFilter();
  }

  @Test
  void shouldAddSecurityHeadersForHttpRequest() throws IOException, ServletException {
    // Given
    when(request.getScheme()).thenReturn("http");
    when(request.getRequestURI()).thenReturn("/api/jobs");

    // When
    securityHeadersFilter.doFilter(request, response, filterChain);

    // Then
    verify(response).setHeader("X-Content-Type-Options", "nosniff");
    verify(response).setHeader("X-Frame-Options", "DENY");
    verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    verify(response)
        .setHeader(
            "Content-Security-Policy",
            "default-src 'none'; script-src 'none'; object-src 'none'; base-uri 'none';");
    verify(response).setHeader("Referrer-Policy", "no-referrer");
    verify(response)
        .setHeader(
            "Permissions-Policy",
            "camera=(), microphone=(), geolocation=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()");
    verify(response).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    verify(response).setHeader("Pragma", "no-cache");
    verify(response).setHeader("Expires", "0");
    verify(response).setHeader("Server", "PDF-Processor-API");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldAddHstsHeaderForHttpsRequest() throws IOException, ServletException {
    // Given
    when(request.getScheme()).thenReturn("https");
    when(request.getRequestURI()).thenReturn("/api/jobs");

    // When
    securityHeadersFilter.doFilter(request, response, filterChain);

    // Then
    verify(response)
        .setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotAddCacheHeadersForNonApiEndpoints() throws IOException, ServletException {
    // Given
    when(request.getScheme()).thenReturn("http");
    when(request.getRequestURI()).thenReturn("/health");

    // When
    securityHeadersFilter.doFilter(request, response, filterChain);

    // Then
    verify(response).setHeader("X-Content-Type-Options", "nosniff");
    verify(response).setHeader("X-Frame-Options", "DENY");
    verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    verify(response).setHeader("Server", "PDF-Processor-API");
    
    // Cache headers should not be set for non-API endpoints
    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(response, org.mockito.Mockito.atLeastOnce()).setHeader(headerCaptor.capture(), valueCaptor.capture());
    
    // Verify cache headers are not present
    java.util.List<String> headers = headerCaptor.getAllValues();
    org.junit.jupiter.api.Assertions.assertFalse(headers.contains("Cache-Control"));
    org.junit.jupiter.api.Assertions.assertFalse(headers.contains("Pragma"));
    org.junit.jupiter.api.Assertions.assertFalse(headers.contains("Expires"));
    
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotAddHstsHeaderForHttpRequest() throws IOException, ServletException {
    // Given
    when(request.getScheme()).thenReturn("http");
    when(request.getRequestURI()).thenReturn("/api/jobs");

    // When
    securityHeadersFilter.doFilter(request, response, filterChain);

    // Then
    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(response, org.mockito.Mockito.atLeastOnce()).setHeader(headerCaptor.capture(), valueCaptor.capture());
    
    // Verify HSTS header is not present for HTTP
    java.util.List<String> headers = headerCaptor.getAllValues();
    org.junit.jupiter.api.Assertions.assertFalse(headers.contains("Strict-Transport-Security"));
    
    verify(filterChain).doFilter(request, response);
  }
}