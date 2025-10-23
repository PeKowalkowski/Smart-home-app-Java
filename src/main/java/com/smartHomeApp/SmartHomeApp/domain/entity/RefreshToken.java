package com.smartHomeApp.SmartHomeApp.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "token", nullable = false, unique = true, length = 128)
  private String token;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @Column(name = "replaced_by_token_id")
  private Long replacedByTokenId;

}
