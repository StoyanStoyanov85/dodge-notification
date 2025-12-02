package com.dodge_notification.exception;

public class NoResourceFoundException extends RuntimeException {

  public NoResourceFoundException() {
    super("Resource not found");
  }
}
