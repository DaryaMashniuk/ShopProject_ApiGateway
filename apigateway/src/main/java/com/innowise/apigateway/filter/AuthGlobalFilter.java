package com.innowise.apigateway.filter;

import com.innowise.apigateway.client.AuthServiceClient;
import com.innowise.apigateway.exception.AuthServiceIsDownException;
import com.innowise.apigateway.exception.TokenValidationException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

  @Value("${authservice.whitelist}")
  private final List<String> whitelist;

  private final AuthServiceClient authServiceClient;
  private static final Logger logger = LogManager.getLogger(AuthGlobalFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().value();

    if (isWhitelisted(path)) {
      return chain.filter(exchange);
    }

    String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return unauthorized(exchange,"Missing or invalid Authorization header");
    }

    String token = authorization.substring(7);
    logger.info("Processing path "+path);
    return authServiceClient.validate(token)
            .flatMap(validation -> {
              if (!validation.isValid()){
                return unauthorized(exchange,"Invalid token");
              }
              logger.info("Validation result: valid={}, userId={}, role={}",
                      validation.isValid(), validation.getUserId(), validation.getRole());


              ServerHttpRequest mutatedRequest = request
                      .mutate()
                      .header("X-User-Id", String.valueOf(validation.getUserId()))
                      .header("X-User-Role",validation.getRole())
                      .build();
              return chain.filter(exchange.mutate().request(mutatedRequest).build());
            })
            .onErrorResume(TokenValidationException.class, e ->
                    unauthorized(exchange,e.getMessage()))
            .onErrorResume(AuthServiceIsDownException.class,e->
                    serverError(exchange,e.getMessage()));
  }

  private boolean isWhitelisted(String path) {
    return whitelist.stream().anyMatch(path::equals);
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");
    byte[] bytes = ("{\"error\": \"" + message + "\"}" ).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  private Mono<Void> serverError(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    byte[] bytes = ("{\"error\": \"" + message + "\"}" ).getBytes(StandardCharsets.UTF_8);
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
