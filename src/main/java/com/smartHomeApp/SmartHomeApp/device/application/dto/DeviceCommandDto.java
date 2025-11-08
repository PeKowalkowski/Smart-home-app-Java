package com.smartHomeApp.SmartHomeApp.device.application.dto;
import jakarta.validation.constraints.NotBlank;

public record DeviceCommandDto(
  @NotBlank String deviceId,
  @NotBlank String command,
  String payload
) {}
