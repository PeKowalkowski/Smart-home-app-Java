package com.smartHomeApp.SmartHomeApp.infrastructure.db.repository;

import com.smartHomeApp.SmartHomeApp.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String tokenHash);

  @Modifying
  @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.userId = :userId")
  void revokeAllByUserId(Long userId);

  List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

  void deleteByExpiresAtBefore(java.time.Instant cutoff);
}
