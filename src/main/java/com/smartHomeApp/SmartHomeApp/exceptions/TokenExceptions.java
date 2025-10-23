package com.smartHomeApp.SmartHomeApp.exceptions;

public class TokenExceptions extends RuntimeException {
  public TokenExceptions(String message) { super(message); }

  public static class RefreshTokenNotFoundException extends TokenExceptions {
    public RefreshTokenNotFoundException() { super("Refresh token not found"); }
  }

  public static class RefreshTokenInvalidException extends TokenExceptions {
    public RefreshTokenInvalidException(String info) { super("Invalid refresh token: " + info); }
  }

  public static class RefreshTokenExpiredException extends TokenExceptions {
    public RefreshTokenExpiredException() { super("Refresh token expired"); }
  }
}
