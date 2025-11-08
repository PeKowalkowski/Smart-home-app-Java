package com.smartHomeApp.SmartHomeApp.device.domain.entity;

import com.smartHomeApp.SmartHomeApp.device.domain.valueobjects.DeviceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "devices", indexes = {
  @Index(name = "idx_device_device_id", columnList = "device_id"),
  @Index(name = "idx_device_user_id", columnList = "user_id"),
  @Index(name = "idx_device_topic_prefix", columnList = "topic_prefix")
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

  @Column(name = "topic_prefix", nullable = false)
  private String topicPrefix;

  @Column(name = "last_seen")
  private Instant lastSeen;

  @Column(name = "firmware_version")
  private String firmwareVersion;

  @Column(name = "location")
  private String location;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  private Instant updatedAt;


  public boolean isOnline(long offlineThresholdSeconds) {
    if (lastSeen == null) return false;
    return lastSeen.isAfter(Instant.now().minusSeconds(offlineThresholdSeconds));
  }

  public String getStatusTopic() {
    return topicPrefix + "/status";
  }

  public String getCommandTopic() {
    return topicPrefix + "/command";
  }

  public String getTelemetryTopic() {
    return topicPrefix + "/telemetry";
  }
}
