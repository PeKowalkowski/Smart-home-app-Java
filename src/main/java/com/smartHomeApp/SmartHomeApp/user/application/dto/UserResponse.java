package com.smartHomeApp.SmartHomeApp.user.application.dto;

import java.time.LocalDateTime;

public record UserResponse(
  Long id,
  String email,
  String username,
  String role,
  LocalDateTime registrationDate,
  LocalDateTime lastLogin,
  boolean active,
  int failedLoginAttempts,
  boolean locked,
  LocalDateTime updatedAt
) {}
