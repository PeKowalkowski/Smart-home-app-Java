package com.smartHomeApp.SmartHomeApp.user.application.mapper;

import com.smartHomeApp.SmartHomeApp.user.application.dto.UserResponse;
import com.smartHomeApp.SmartHomeApp.user.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.RegisterRequest;

import java.time.LocalDateTime;

public final class UserMapper {

  private UserMapper() {
  }

  public static User toEntity(RegisterRequest req, String hashedPassword) {
    return User.builder()
      .email(req.email())
      .username(req.username())
      .password(hashedPassword)
      .role(null)
      .active(true)
      .registrationDate(LocalDateTime.now())
      .failedLoginAttempts(0)
      .locked(false)
      .build();
  }
  public static UserResponse toResponse(User user) {
    if (user == null) return null;
    return new UserResponse(
      user.getId(),
      user.getEmail(),
      user.getUsername(),
      user.getRole() != null ? user.getRole().name() : null,
      user.getRegistrationDate(),
      user.getLastLogin(),
      user.isActive(),
      user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0,
      user.isLocked(),
      user.getUpdatedAt()
    );
  }
}
