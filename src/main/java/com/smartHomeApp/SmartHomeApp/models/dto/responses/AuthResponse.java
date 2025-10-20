package com.smartHomeApp.SmartHomeApp.models.dto.responses;

import java.time.LocalDateTime;

public record AuthResponse(
  String token,
  String email,
  String username,
  LocalDateTime lastLogin,
  String message
) {
}
