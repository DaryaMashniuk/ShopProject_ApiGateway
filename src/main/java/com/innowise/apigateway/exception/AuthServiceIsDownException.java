package com.innowise.apigateway.exception;

public class AuthServiceIsDownException extends RuntimeException {
  public AuthServiceIsDownException() {
    super();
  }
  public AuthServiceIsDownException(String message) {
    super(message);
  }
  public AuthServiceIsDownException(String message, Throwable cause) {
    super(message, cause);
  }
}
