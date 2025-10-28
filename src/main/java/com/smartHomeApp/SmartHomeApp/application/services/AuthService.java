package com.smartHomeApp.SmartHomeApp.application.services;

import com.smartHomeApp.SmartHomeApp.config.jwt.JwtProperties;
import com.smartHomeApp.SmartHomeApp.config.jwt.JwtTokenService;
import com.smartHomeApp.SmartHomeApp.config.jwt.TokenHash;
import com.smartHomeApp.SmartHomeApp.domain.entity.RefreshToken;
import com.smartHomeApp.SmartHomeApp.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.domain.enums.Role;
import com.smartHomeApp.SmartHomeApp.exceptions.BusinessExceptions;
import com.smartHomeApp.SmartHomeApp.exceptions.TokenExceptions;
import com.smartHomeApp.SmartHomeApp.infrastructure.db.repository.RefreshTokenRepository;
import com.smartHomeApp.SmartHomeApp.infrastructure.db.repository.UserRepository;
import com.smartHomeApp.SmartHomeApp.models.dto.requests.LoginRequest;
import com.smartHomeApp.SmartHomeApp.models.dto.requests.RegisterRequest;
import com.smartHomeApp.SmartHomeApp.models.dto.responses.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtProperties jwtProperties;

  private static final int MAX_ACTIVE_REFRESH_TOKENS = 5;


  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService, RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtProperties = jwtProperties;
  }

  @Transactional
  public AuthResponse registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessExceptions.UserAlreadyExistsException(request.email());
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new BusinessExceptions.UserAlreadyExistsException(request.username());
    }

    var newUser = User.builder()
      .email(request.email())
      .username(request.username())
      .password(passwordEncoder.encode(request.password()))
      .role(Role.USER)
      .active(true)
      .registrationDate(LocalDateTime.now())
      .build();

    var savedUser = userRepository.save(newUser);

    refreshTokenRepository.deleteExpiredTokens(Instant.now());

    maintainActiveTokenLimit(savedUser.getId());

    var accessToken = jwtTokenService.generateAccessToken(savedUser);
    var refreshPlain = jwtTokenService.generateRefreshToken(savedUser);
    var refreshHash = TokenHash.sha256(refreshPlain);

    maintainActiveTokenLimit(savedUser.getId());

    var now = Instant.now();
    var refresh = RefreshToken.builder()
      .tokenHash(refreshHash)
      .userId(savedUser.getId())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(refresh);

    log.info("User registered: {}", savedUser.getEmail());
    return new AuthResponse(accessToken, refreshPlain, savedUser.getEmail(), savedUser.getUsername(), savedUser.getLastLogin(), "User registered successfully");
  }

  @Transactional
  public AuthResponse loginUser(LoginRequest request) {
    var user = userRepository.findByEmail(request.email())
      .or(() -> userRepository.findByUsername(request.email()))
      .orElseThrow(BusinessExceptions.InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BusinessExceptions.InvalidCredentialsException();
    }

    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);

    refreshTokenRepository.deleteExpiredTokens(Instant.now());

    refreshTokenRepository.revokeAllByUserId(user.getId());

    var accessToken = jwtTokenService.generateAccessToken(user);
    var refreshPlain = jwtTokenService.generateRefreshToken(user);
    var refreshHash = TokenHash.sha256(refreshPlain);

    var now = Instant.now();
    var refresh = RefreshToken.builder()
      .tokenHash(refreshHash)
      .userId(user.getId())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(refresh);

    log.info("User logged in: {}", user.getEmail());
    return new AuthResponse(accessToken, refreshPlain, user.getEmail(), user.getUsername(), user.getLastLogin(), "User logged in successfully");
  }

  @Transactional
  public AuthResponse refreshToken(String refreshTokenValue) {
    log.info("Refreshing access token...");

    refreshTokenRepository.deleteExpiredTokens(Instant.now());

    var refreshHash = TokenHash.sha256(refreshTokenValue);
    var refreshToken = refreshTokenRepository.findByTokenHash(refreshHash)
      .orElseThrow(TokenExceptions.RefreshTokenNotFoundException::new);

    if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.revokeAllByUserId(refreshToken.getUserId());
      throw new TokenExceptions.RefreshTokenInvalidException("token revoked or expired");
    }

    var user = userRepository.findById(refreshToken.getUserId())
      .orElseThrow(() -> new BusinessExceptions.UserNotFoundException(refreshToken.getUserId()));

    var newAccessToken = jwtTokenService.generateAccessToken(user);

    refreshTokenRepository.revokeByIds(List.of(refreshToken.getId()));

    var newRefreshPlain = jwtTokenService.generateRefreshToken(user);
    var newRefreshHash = TokenHash.sha256(newRefreshPlain);
    var now = Instant.now();
    var newRefresh = RefreshToken.builder()
      .tokenHash(newRefreshHash)
      .userId(user.getId())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(newRefresh);

    return new AuthResponse(newAccessToken, newRefreshPlain, user.getEmail(), user.getUsername(), user.getLastLogin(), "Access token refreshed successfully");
  }

  private void maintainActiveTokenLimit(Long userId) {
    refreshTokenRepository.deleteExpiredTokens(Instant.now());

    var active = refreshTokenRepository.findActiveByUserId(userId);
    if (active.size() >= MAX_ACTIVE_REFRESH_TOKENS) {
      int toRevokeCount = active.size() - (MAX_ACTIVE_REFRESH_TOKENS - 1);
      var toRevoke = active.subList(active.size() - toRevokeCount, active.size());
      var idsToRevoke = toRevoke.stream().map(RefreshToken::getId).collect(Collectors.toList());
      if (!idsToRevoke.isEmpty()) {
        refreshTokenRepository.revokeByIds(idsToRevoke);
        log.info("Revoked {} refresh token(s) for userId={}", idsToRevoke.size(), userId);
      }
    }
  }
}

