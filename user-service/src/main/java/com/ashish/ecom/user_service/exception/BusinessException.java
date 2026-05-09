package com.ashish.ecom.user_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
  private final HttpStatus status;
  private final String errorCode;

  protected BusinessException(String message, HttpStatus status, String errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
}
