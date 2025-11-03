package com.smartHomeApp.SmartHomeApp.device.domain.entity;

import com.smartHomeApp.SmartHomeApp.device.domain.valueobjects.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "devices", indexes = {
  @Index(name = "idx_device_deviceId", columnList = "device_id"),
  @Index(name = "idx_device_userId", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false, unique = true)
  private String deviceId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DeviceStatus status = DeviceStatus.UNKNOWN;


  @Column(name = "last_value")
  private String lastValue;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String topic;

  @Column(name = "last_seen")
  private Instant lastSeen;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  private Instant updatedAt;

  public boolean isOnline(long offlineThresholdSeconds) {
    if (lastSeen == null) return false;
    return lastSeen.isAfter(Instant.now().minusSeconds(offlineThresholdSeconds));
  }
}
