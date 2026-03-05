package com.innowise.apigateway.client;

import com.innowise.apigateway.exception.TokenValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.innowise.apigateway.model.ValidationResponseDto;

import java.util.Map;

@Component
public class AuthServiceClient {

  private WebClient webClient;

  public AuthServiceClient(@Value("${authservice.url:http://localhost:8082}") String baseUrl) {
    this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
  }


  public Mono<ValidationResponseDto> validate(String token) {
    return webClient.post()
            .uri("/api/v1/auth/validate")
            .bodyValue(Map.of("token", token))
            .retrieve()
            .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> Mono.error(new TokenValidationException("Invalid token"))
            )
            .bodyToMono(ValidationResponseDto.class);
  }
}
