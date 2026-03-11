package com.innowise.apigateway.client;

import com.innowise.apigateway.exception.AuthServiceIsDownException;
import com.innowise.apigateway.exception.TokenValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.innowise.apigateway.model.ValidationResponseDto;

import java.util.Map;

@Component
public class AuthServiceClient {

  private WebClient webClient;

  public AuthServiceClient(@Value("${authservice.url}") String baseUrl) {
    this.webClient = WebClient.builder()
            .baseUrl(baseUrl.trim())
            .build();
  }


  public Mono<ValidationResponseDto> validate(String token) {
    return webClient.post()
            .uri("/api/v1/auth/validate")
            .bodyValue(Map.of("token", token))
            .retrieve()
            .onStatus(
                    HttpStatusCode::is4xxClientError,
                    response -> Mono.error(new TokenValidationException("Invalid token"))
            )
            .onStatus(
                    HttpStatusCode::is5xxServerError,
                    response -> Mono.error(new AuthServiceIsDownException("Auth service is down"))
            )
            .bodyToMono(ValidationResponseDto.class);
  }
}