package com.smartHomeApp.SmartHomeApp.user.domain.entity;

import com.smartHomeApp.SmartHomeApp.user.domain.value.Role;
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

  @Column(nullable = false)
  private boolean active = true;



  @Column(name = "failed_login_attempts", nullable = false)
  private Integer failedLoginAttempts = 0;

  @Column(name = "locked", nullable = false)
  private Boolean locked = false;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public boolean isActive() { return Boolean.TRUE.equals(active); }
  public boolean isLocked() { return Boolean.TRUE.equals(locked); }

  public Boolean getActive() {
    return this.active;
  }

}
