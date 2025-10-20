package com.smartHomeApp.SmartHomeApp.domain.entity;

import com.smartHomeApp.SmartHomeApp.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Email(message = "Email must be valid")
  @NotBlank(message = "Email cannot be blank")
  @Column(unique = true, nullable = false)
  private String email;

  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Column(unique = true)
  private String username;

  @NotBlank(message = "Password cannot be blank")
  @Size(min = 6, message = "Password must be at least 6 characters")
  @Column(nullable = false)
  private String password;

  @NotNull(message = "Role cannot be null")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @NotNull
  @Column(nullable = false)
  private LocalDateTime registrationDate = LocalDateTime.now();

  private LocalDateTime lastLogin;

  private boolean active = true;
}
