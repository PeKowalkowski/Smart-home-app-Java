package com.smartHomeApp.SmartHomeApp.config.jwt;

import com.smartHomeApp.SmartHomeApp.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final JwtProperties jwtProperties;

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

}

