package com.innowise.apigateway.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class ValidationResponseDto {

  private boolean valid;

  private String role;

  private Long userId;

  private Date expiresAt;
}
