package com.smartHomeApp.SmartHomeApp.device.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "device_sensors", indexes = {
  @Index(name = "idx_sensor_device_id", columnList = "device_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "device_id", nullable = false)
  private String deviceId;

  @Column(nullable = false)
  private String type;

  @Column(name = "last_value", columnDefinition = "text")
  private String lastValue;

  @Column(name = "last_seen")
  private Instant lastSeen;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
