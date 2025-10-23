package com.smartHomeApp.SmartHomeApp.models.dto;

import java.time.Instant;

public record RefreshTokenDto(String token, Instant expiresAt) { }
