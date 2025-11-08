package com.smartHomeApp.SmartHomeApp.device.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceRequestDto(
  @NotBlank String deviceId,
  @NotBlank String name,
  @NotBlank String type,
  @NotBlank String topicPrefix,
  @NotNull Long userId,
  String location
) {}
