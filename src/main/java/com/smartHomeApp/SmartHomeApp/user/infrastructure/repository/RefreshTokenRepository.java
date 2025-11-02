package com.smartHomeApp.SmartHomeApp.user.infrastructure.repository;

import com.smartHomeApp.SmartHomeApp.user.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
  void revokeAllByUserId(@Param("userId") Long userId);

  @Query("SELECT r FROM RefreshToken r WHERE r.userId = :userId AND r.revoked = false ORDER BY r.issuedAt DESC")
  List<RefreshToken> findActiveByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :cutoff")
  int deleteExpiredTokens(@Param("cutoff") Instant cutoff);

  @Modifying(clearAutomatically = true)
  @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.id IN :ids")
  void revokeByIds(@Param("ids") List<Long> ids);
}
