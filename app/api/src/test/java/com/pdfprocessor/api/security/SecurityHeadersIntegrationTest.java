package com.pdfprocessor.api.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SecurityHeadersIntegrationTest {

  private final SecurityHeadersFilter securityHeadersFilter = new SecurityHeadersFilter();

  @Test
  void shouldApplySecurityHeadersToApiEndpoints() throws ServletException, IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    when(request.getRequestURI()).thenReturn("/api/jobs");
    when(request.getScheme()).thenReturn("http");

    securityHeadersFilter.doFilter(request, response, filterChain);

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
  void shouldApplyHstsHeaderForHttpsRequests() throws ServletException, IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    when(request.getRequestURI()).thenReturn("/api/jobs");
    when(request.getScheme()).thenReturn("https");

    securityHeadersFilter.doFilter(request, response, filterChain);

    verify(response).setHeader("X-Content-Type-Options", "nosniff");
    verify(response).setHeader("X-Frame-Options", "DENY");
    verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    verify(response)
        .setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotApplyCacheHeadersToNonApiEndpoints() throws ServletException, IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    when(request.getRequestURI()).thenReturn("/health");
    when(request.getScheme()).thenReturn("http");

    securityHeadersFilter.doFilter(request, response, filterChain);

    verify(response).setHeader("X-Content-Type-Options", "nosniff");
    verify(response).setHeader("X-Frame-Options", "DENY");
    verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    // Verify cache headers are NOT set for non-API endpoints
    verify(response, org.mockito.Mockito.never()).setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    verify(response, org.mockito.Mockito.never()).setHeader("Pragma", "no-cache");
    verify(response, org.mockito.Mockito.never()).setHeader("Expires", "0");
    verify(filterChain).doFilter(request, response);
  }
}