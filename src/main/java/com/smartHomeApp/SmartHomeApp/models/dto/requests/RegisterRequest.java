package com.smartHomeApp.SmartHomeApp.models.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @Email(message = "Email must be valid")
  @NotBlank(message = "Email cannot be blank")
  String email,

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters")
  String username,

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 32, message = "Password must be between 8 and 100 characters")
  String password
) {

}
