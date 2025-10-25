package com.smartHomeApp.SmartHomeApp.application.services;

import com.smartHomeApp.SmartHomeApp.config.jwt.JwtProperties;
import com.smartHomeApp.SmartHomeApp.config.jwt.JwtTokenService;
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

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService, RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtProperties = jwtProperties;
  }

 public AuthResponse registerUser(RegisterRequest request) {
   log.info("Registering new user: {}", request.email());

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

   var accessToken = jwtTokenService.generateAccessToken(savedUser);
   var refreshTokenValue = jwtTokenService.generateRefreshToken(savedUser);

   var refreshToken = RefreshToken.builder()
     .token(refreshTokenValue)
     .userId(savedUser.getId())
     .issuedAt(Instant.now())
     .expiresAt(Instant.now().plus(jwtProperties.refreshToken()))
     .revoked(false)
     .build();
   refreshTokenRepository.save(refreshToken);

   return new AuthResponse(
     accessToken,
     refreshTokenValue,
     savedUser.getEmail(),
     savedUser.getUsername(),
     savedUser.getLastLogin(),
     "User registered successfully"
   );
 }

  public AuthResponse loginUser(LoginRequest request) {
    log.info("Login attempt for user: {}", request.email());

    var user = userRepository.findByEmail(request.email())
      .or(() -> userRepository.findByUsername(request.email()))
      .orElseThrow(BusinessExceptions.InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BusinessExceptions.InvalidCredentialsException();
    }

    user.setLastLogin(LocalDateTime.now());
    userRepository.save(user);

    var accessToken = jwtTokenService.generateAccessToken(user);
    var refreshTokenValue = jwtTokenService.generateRefreshToken(user);

    refreshTokenRepository.revokeAllByUserId(user.getId());

    var refreshToken = RefreshToken.builder()
      .token(refreshTokenValue)
      .userId(user.getId())
      .issuedAt(Instant.now())
      .expiresAt(Instant.now().plus(jwtProperties.refreshToken()))
      .revoked(false)
      .build();
    refreshTokenRepository.save(refreshToken);

    return new AuthResponse(
      accessToken,
      refreshTokenValue,
      user.getEmail(),
      user.getUsername(),
      user.getLastLogin(),
      "User logged in successfully"
    );
  }

  public AuthResponse refreshToken(String refreshTokenValue) {
    log.info("Refreshing access token...");

    var refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
      .orElseThrow(TokenExceptions.RefreshTokenNotFoundException::new);

    if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(Instant.now())) {
      throw new TokenExceptions.RefreshTokenInvalidException("token revoked or expired");
    }

    var user = userRepository.findById(refreshToken.getUserId())
      .orElseThrow(() -> new BusinessExceptions.UserNotFoundException(refreshToken.getUserId()));
    var newAccessToken = jwtTokenService.generateAccessToken(user);

    return new AuthResponse(
      newAccessToken,
      refreshToken.getToken(),
      user.getEmail(),
      user.getUsername(),
      user.getLastLogin(),
      "Access token refreshed successfully"
    );
  }
}

