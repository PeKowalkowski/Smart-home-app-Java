package com.smartHomeApp.SmartHomeApp.infrastructure.db.repository;

import com.smartHomeApp.SmartHomeApp.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String tokenHash);

  List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);

  void deleteByExpiresAtBefore(java.time.Instant cutoff);
}
