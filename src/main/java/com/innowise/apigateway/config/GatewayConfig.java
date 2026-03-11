package com.innowise.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

  @Value("${authservice.url}")
  private String authServiceUrl;

  @Value("${orderservice.url}")
  private String orderServiceUrl;

  @Value("${userservice.url}")
  private String userServiceUrl;

  @Bean
  public RouteLocator customRoutes(RouteLocatorBuilder builder) {
    String authUrl = authServiceUrl.trim();
    String userUrl = userServiceUrl.trim();
    String orderUrl = orderServiceUrl.trim();
    return builder.routes()
            .route("auth-public", r -> r
                    .path("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh")
                    .filters(f -> f.stripPrefix(0))
                    .uri(authUrl))
            .route("user-service", r -> r
                    .path("/api/v1/users/**")
                    .filters(f -> f
                            .rewritePath("/api/v1/users(?<segment>/?.*)", "/userservice/api/v1/users${segment}")
                    )
                    .uri(userUrl))
            .route("auth-protected", r -> r
                    .path("/api/v1/auth/**")
                    .filters(f -> f.stripPrefix(0))
                    .uri(authUrl))
            .route("order-service", r -> r
                    .path("/api/v1/orders/**")
                    .filters(f -> f.stripPrefix(0))
                    .uri(orderUrl))
            .build();

  }
}
