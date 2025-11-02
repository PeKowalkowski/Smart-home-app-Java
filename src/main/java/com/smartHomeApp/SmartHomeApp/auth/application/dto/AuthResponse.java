package com.smartHomeApp.SmartHomeApp.auth.application.dto;

import java.time.LocalDateTime;

public record AuthResponse(
  String accessToken,
  String refreshToken,
  String email,
  String username,
  LocalDateTime lastLogin,
  String message
) {
}
