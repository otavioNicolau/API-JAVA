package com.pdfprocessor.api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Configuração de segurança da API. Implementa autenticação via X-API-Key header. */
@Configuration
@EnableWebSecurity
@ConfigurationProperties(prefix = "app.security")
public class SecurityConfig {

  private List<String> apiKeys;

  public List<String> getApiKeys() {
    return apiKeys;
  }

  public void setApiKeys(List<String> apiKeys) {
    this.apiKeys = apiKeys;
  }

  @Bean
  public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter() {
    return new ApiKeyAuthenticationFilter(apiKeys.toArray(new String[0]));
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint() {
    return (HttpServletRequest request, HttpServletResponse response, 
            org.springframework.security.core.AuthenticationException authException) -> {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType("application/json");
      response.getWriter().write(
          "{\"error\":\"Unauthorized\",\"message\":\"API key required\",\"status\":401}");
      response.getWriter().flush();
    };
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              System.out.println("[DEBUG] AccessDeniedHandler chamado: " + accessDeniedException.getMessage());
              System.out.println("[DEBUG] Request URI: " + request.getRequestURI());
              System.out.println("[DEBUG] Authentication: " + org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
              response.setStatus(HttpStatus.FORBIDDEN.value());
              response.setContentType("application/json");
              response.getWriter().write(
                  "{\"error\":\"Forbidden\",\"message\":\"Access denied\",\"status\":403}");
              response.getWriter().flush();
            }))
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/actuator/health", "/h2-console/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
            .requestMatchers("/api/v1/**").hasRole("API_USER")
            .anyRequest().authenticated())
        .addFilterBefore(apiKeyAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
