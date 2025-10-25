package com.smartHomeApp.SmartHomeApp.exceptions;

public class BusinessExceptions extends RuntimeException {
  public BusinessExceptions(String message) {
    super(message);
  }

  public static class UserAlreadyExistsException extends BusinessExceptions {
    public UserAlreadyExistsException(String email) {
      super("User with email " + email + " already exists: " + email);
    }
  }
  public static class UserNotFoundException extends BusinessExceptions {
    public UserNotFoundException(Long id) {
      super("User with id  " + id + " don't exists.");
    }
  }

  public static class InvalidCredentialsException extends BusinessExceptions {
    public InvalidCredentialsException() {
      super("Invalid username or password");
    }
  }
}
