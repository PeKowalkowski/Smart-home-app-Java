package com.smartHomeApp.SmartHomeApp.application.services;

import com.smartHomeApp.SmartHomeApp.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.domain.enums.Role;
import com.smartHomeApp.SmartHomeApp.exceptions.BusinessExceptions;
import com.smartHomeApp.SmartHomeApp.infrastructure.db.repository.UserRepository;
import com.smartHomeApp.SmartHomeApp.models.dto.requests.LoginRequest;
import com.smartHomeApp.SmartHomeApp.models.dto.requests.RegisterRequest;
import com.smartHomeApp.SmartHomeApp.models.dto.responses.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

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

      userRepository.save(newUser);

      return new AuthResponse(
        null,
        newUser.getEmail(),
        newUser.getUsername(),
        newUser.getLastLogin(),
        "User registered successfully"
      );
    }

    public AuthResponse loginUser(LoginRequest request) {
      var userOptional = userRepository.findByEmail(request.email())
        .or(() -> userRepository.findByUsername(request.email()));

      var user = userOptional.orElseThrow(() ->
        new BusinessExceptions.InvalidCredentialsException()
      );

      if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        throw new BusinessExceptions.InvalidCredentialsException();
      }

      user.setLastLogin(LocalDateTime.now());
      userRepository.save(user);

      return new AuthResponse(
        null,
        user.getEmail(),
        user.getUsername(),
        user.getLastLogin(),
        "User logged in successfully"
      );
    }

}
