package dev.slethware.pushnotifications.exception;

public class InvalidDeviceTokenException extends RuntimeException {
  public InvalidDeviceTokenException(String message) {
    super(message);
  }
}