package com.smartHomeApp.SmartHomeApp.device.application.dto;

import com.smartHomeApp.SmartHomeApp.device.domain.valueobjects.DeviceStatus;
import java.time.Instant;

public record DeviceResponseDto(
  Long id,
  String deviceId,
  String name,
  String type,
  DeviceStatus status,
  String lastValue,
  String topicPrefix,
  String location,
  Instant lastSeen,
  Instant createdAt
) {}
