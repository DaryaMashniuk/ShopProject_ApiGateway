package com.innowise.apigateway.model;

//import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Date;

@Builder
@Data
@Getter
public class ValidationResponseDto {

  private boolean valid;

  private String role;

  private Long userId;

  private Date expiresAt;
}
