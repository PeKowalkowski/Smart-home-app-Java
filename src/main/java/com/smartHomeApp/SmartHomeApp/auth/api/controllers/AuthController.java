package com.smartHomeApp.SmartHomeApp.auth.api.controllers;

import com.smartHomeApp.SmartHomeApp.auth.application.dto.AuthResponse;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.LoginRequest;
import com.smartHomeApp.SmartHomeApp.auth.application.dto.RegisterRequest;
import com.smartHomeApp.SmartHomeApp.auth.application.services.AuthService;
import com.smartHomeApp.SmartHomeApp.auth.infrastructure.jwt.JwtProperties;
import com.smartHomeApp.SmartHomeApp.common.exceptions.TokenExceptions;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtProperties jwtProperties;

  public AuthController(AuthService authService, JwtProperties jwtProperties) {
    this.authService = authService;
    this.jwtProperties = jwtProperties;
  }


  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
    var auth = authService.registerUser(request);

    var cookie = ResponseCookie.from("refresh_token", auth.refreshToken())
      .httpOnly(true)
      .secure(true)
      .path("/api/auth/refresh")
      .sameSite("Strict")
      .maxAge(jwtProperties.refreshToken().toSeconds())
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    var body = new AuthResponse(auth.accessToken(), null, auth.email(), auth.username(), auth.lastLogin(), auth.message());
    return ResponseEntity.status(201).body(body);
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    var auth = authService.loginUser(request);

    var cookie = ResponseCookie.from("refresh_token", auth.refreshToken())
      .httpOnly(true)
      .secure(true)
      .path("/api/auth/refresh")
      .sameSite("Strict")
      .maxAge(jwtProperties.refreshToken().toSeconds())
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    var body = new AuthResponse(auth.accessToken(), null, auth.email(), auth.username(), auth.lastLogin(), auth.message());
    return ResponseEntity.ok(body);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
    var cookie = Optional.ofNullable(request.getCookies())
      .flatMap(cookies -> Arrays.stream(cookies).filter(c -> "refresh_token".equals(c.getName())).findFirst())
      .orElseThrow(() -> new TokenExceptions.RefreshTokenNotFoundException());

    var auth = authService.refreshToken(cookie.getValue());

    var newCookie = ResponseCookie.from("refresh_token", auth.refreshToken())
      .httpOnly(true)
      .secure(true)
      .path("/api/auth/refresh")
      .sameSite("Strict")
      .maxAge(jwtProperties.refreshToken().toSeconds())
      .build();
    response.setHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

    var body = new AuthResponse(auth.accessToken(), null, auth.email(), auth.username(), auth.lastLogin(), auth.message());
    return ResponseEntity.ok(body);
  }
}
