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

   var saved = userRepository.save(newUser);

   var accessToken = jwtTokenService.generateAccessToken(saved);
   var refreshPlain = jwtTokenService.generateRefreshToken(saved);
   var refreshHash = TokenHash.sha256(refreshPlain);

   maintainActiveTokenLimit(saved.getId());

   var now = Instant.now();
   var refresh = RefreshToken.builder()
     .tokenHash(refreshHash)
     .userId(saved.getId())
     .issuedAt(now)
     .expiresAt(now.plus(jwtProperties.refreshToken()))
     .revoked(false)
     .build();
   refreshTokenRepository.save(refresh);

   log.info("User registered: {}", saved.getEmail());
   return new AuthResponse(accessToken, refreshPlain, saved.getEmail(), saved.getUsername(), saved.getLastLogin(), "User registered successfully");
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
  public AuthResponse refreshToken(String refreshTokenPlain) {
    var hash = TokenHash.sha256(refreshTokenPlain);

    var refreshToken = refreshTokenRepository.findByTokenHash(hash)
      .orElseThrow(() -> new TokenExceptions.RefreshTokenNotFoundException());

    if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
      throw new TokenExceptions.RefreshTokenInvalidException("refresh token invalid or expired");
    }

    var user = userRepository.findById(refreshToken.getUserId())
      .orElseThrow(() -> new BusinessExceptions.UserNotFoundException(refreshToken.getUserId()));

    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);

    var newRefreshPlain = jwtTokenService.generateRefreshToken(user);
    var newHash = TokenHash.sha256(newRefreshPlain);
    var now = Instant.now();
    var newRefresh = RefreshToken.builder()
      .tokenHash(newHash)
      .userId(user.getId())
      .issuedAt(now)
      .expiresAt(now.plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(newRefresh);

    var newAccess = jwtTokenService.generateAccessToken(user);

    log.info("Refresh token rotated for userId={}", user.getId());
    return new AuthResponse(newAccess, newRefreshPlain, user.getEmail(), user.getUsername(), user.getLastLogin(), "Access token refreshed");
  }
  private void maintainActiveTokenLimit(Long userId) {
    var active = refreshTokenRepository.findActiveByUserId(userId);
    if (active.size() >= MAX_ACTIVE_REFRESH_TOKENS) {
      int toRevokeCount = active.size() - (MAX_ACTIVE_REFRESH_TOKENS - 1);
      var toRevoke = active.subList(active.size() - toRevokeCount, active.size());
      toRevoke.forEach(t -> t.setRevoked(true));
      refreshTokenRepository.saveAll(toRevoke);
    }
  }
}

