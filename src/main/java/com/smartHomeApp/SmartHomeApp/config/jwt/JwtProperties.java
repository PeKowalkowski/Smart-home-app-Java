package com.smartHomeApp.SmartHomeApp.config.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
  String issuer,
  Duration accessToken,
  Duration refreshToken
) {}
