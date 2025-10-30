package com.smartHomeApp.SmartHomeApp.application.services;

import com.smartHomeApp.SmartHomeApp.infrastructure.jwt.JwtProperties;
import com.smartHomeApp.SmartHomeApp.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.infrastructure.db.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;
  private final RefreshTokenRepository refreshTokenRepository;

  public String generateAccessToken(User user) {
    var now = Instant.now();
    var claims = JwtClaimsSet.builder()
      .issuer(jwtProperties.issuer())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.accessToken()))
      .subject(user.getEmail())
      .claim("role", user.getRole().name())
      .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
  public String generateRefreshToken(User user) {
    var now = Instant.now();
    var claims = JwtClaimsSet.builder()
      .issuer(jwtProperties.issuer())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .subject(user.getEmail())
      .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
  @Scheduled(cron = "${jwt.cleanup.cron:0 30 2 * * *}")
  @Transactional
  public void deleteExpiredTokensScheduled() {
    var now = Instant.now();
    int deleted = refreshTokenRepository.deleteExpiredTokens(now);
    if (deleted > 0) {
      System.out.println("Deleted " + deleted + " expired refresh tokens");
    }
  }

}

