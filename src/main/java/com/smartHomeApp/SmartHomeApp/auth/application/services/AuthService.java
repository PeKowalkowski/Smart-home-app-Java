package com.smartHomeApp.SmartHomeApp.auth.application.services;

import com.smartHomeApp.SmartHomeApp.auth.infrastructure.jwt.JwtProperties;
import com.smartHomeApp.SmartHomeApp.auth.infrastructure.jwt.TokenHash;
import com.smartHomeApp.SmartHomeApp.user.domain.aggregates.UserAggregate;
import com.smartHomeApp.SmartHomeApp.user.domain.entity.RefreshToken;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserRegisteredEvent;
import com.smartHomeApp.SmartHomeApp.user.domain.value.Role;
import com.smartHomeApp.SmartHomeApp.common.exceptions.BusinessExceptions;
import com.smartHomeApp.SmartHomeApp.common.exceptions.TokenExceptions;
import com.smartHomeApp.SmartHomeApp.user.infrastructure.repository.RefreshTokenRepository;
import com.smartHomeApp.SmartHomeApp.user.infrastructure.repository.UserRepository;
import com.smartHomeApp.SmartHomeApp.user.application.mapper.UserMapper;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.LoginRequest;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.RegisterRequest;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
  private final ApplicationEventPublisher eventPublisher;

  private static final int MAX_ACTIVE_REFRESH_TOKENS = 5;

  public AuthService(UserRepository userRepository,
                     PasswordEncoder passwordEncoder,
                     JwtTokenService jwtTokenService,
                     RefreshTokenRepository refreshTokenRepository,
                     JwtProperties jwtProperties,
                     ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtProperties = jwtProperties;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public AuthResponse registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessExceptions.UserAlreadyExistsException(request.email());
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new BusinessExceptions.UserAlreadyExistsException(request.username());
    }

    String hashed = passwordEncoder.encode(request.password());
    var newUser = UserMapper.toEntity(request, hashed);
    newUser.setRole(Role.USER);

    var agg = UserAggregate.registerNew(newUser);
    var savedUser = userRepository.save(agg.getUser());

    eventPublisher.publishEvent(new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail()));

    refreshTokenRepository.deleteExpiredTokens(Instant.now());

    maintainActiveTokenLimit(savedUser.getId());

    var accessToken = jwtTokenService.generateAccessToken(savedUser);
    var refreshPlain = jwtTokenService.generateRefreshToken(savedUser);
    var refreshHash = TokenHash.sha256(refreshPlain);

    var now = Instant.now();
    var refresh = RefreshToken.builder()
      .tokenHash(refreshHash)
      .userId(savedUser.getId())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(refresh);

    agg.pullDomainEvents().forEach(eventPublisher::publishEvent);

    log.info("User registered: {}", savedUser.getEmail());
    return new AuthResponse(accessToken, refreshPlain, savedUser.getEmail(), savedUser.getUsername(), savedUser.getLastLogin(), "User registered successfully");
  }

    @Transactional
    public AuthResponse loginUser(LoginRequest request) {
      var user = userRepository.findByEmail(request.email())
        .or(() -> userRepository.findByUsername(request.email()))
        .orElseThrow(BusinessExceptions.InvalidCredentialsException::new);

      if (Boolean.TRUE.equals(user.getLocked()) || !Boolean.TRUE.equals(user.getActive())) {
        throw new BusinessExceptions.UserLockedOrInactiveException(user.getId());
      }

      if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        var aggFail = new UserAggregate(user);
        aggFail.onFailedLoginAttempt();
        userRepository.save(aggFail.getUser());
        aggFail.pullDomainEvents().forEach(eventPublisher::publishEvent);
        throw new BusinessExceptions.InvalidCredentialsException();
      }

      var agg = new UserAggregate(user);
      agg.recordLogin();
      userRepository.save(agg.getUser());
      agg.pullDomainEvents().forEach(eventPublisher::publishEvent);

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

    if (Boolean.TRUE.equals(user.getLocked()) || !Boolean.TRUE.equals(user.getActive())) {
      throw new BusinessExceptions.UserLockedOrInactiveException(user.getId());
    }

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

