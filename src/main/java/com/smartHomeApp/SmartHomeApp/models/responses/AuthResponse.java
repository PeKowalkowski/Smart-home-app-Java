package com.smartHomeApp.SmartHomeApp.models.responses;

import java.time.LocalDateTime;

public record AuthResponse(
 /* String token,
  String refreshTokenValue,
  String email,
  String username,
  LocalDateTime lastLogin,
  String message*/
  String accessToken,
  String refreshToken,
  String email,
  String username,
  LocalDateTime lastLogin,
  String message
) {
}
