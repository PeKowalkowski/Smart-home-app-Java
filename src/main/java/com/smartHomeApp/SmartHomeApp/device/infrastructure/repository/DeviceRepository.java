package com.smartHomeApp.SmartHomeApp.device.infrastructure.repository;

import com.smartHomeApp.SmartHomeApp.device.domain.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

  Optional<Device> findByDeviceId(String deviceId);

  List<Device> findAllByUserId(Long userId);

  Optional<Device> findByTopicPrefix(String topicPrefix);
}
