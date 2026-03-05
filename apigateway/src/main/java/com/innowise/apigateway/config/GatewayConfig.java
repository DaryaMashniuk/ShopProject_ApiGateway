package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Value("${authservice.url:http://localhost:8082}")
  private String authServiceUrl;

  @Value("${orderservice.url:http://localhost:8080}")
  private String orderServiceUrl;

  @Value("${userservice.url:http://localhost:8081}")
  private String userServiceUrl;

  @Bean
  public RouteLocator customRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
            .route("auth-public", r -> r
                    .path("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh")
                    .filters(f -> f.stripPrefix(0))
                    .uri(authServiceUrl))
            .route("user-service", r -> r
                    .path("/api/v1/users/**")
                    .filters(f -> f
                            .rewritePath("/api/v1/users(?<segment>/?.*)", "/userservice/api/v1/users${segment}")
                    )
                    .uri(userServiceUrl))
            .route("auth-protected", r -> r
                    .path("/api/v1/auth/**")
                    .filters(f -> f.stripPrefix(0))
                    .uri(authServiceUrl))
            .route("order-service", r -> r
                    .path("/api/v1/orders/**")
                    .filters(f -> f.stripPrefix(0))
                    .uri(orderServiceUrl))
            .build();

  }
}
