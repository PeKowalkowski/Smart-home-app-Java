package com.smartHomeApp.SmartHomeApp.device.domain.events;

import java.time.Instant;

public record DeviceAddedEvent(String deviceId, Long userId, Instant happenedAt) {}
