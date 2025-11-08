package com.smartHomeApp.SmartHomeApp.device.application.services;

import com.smartHomeApp.SmartHomeApp.common.exceptions.DeviceExceptions;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceRequestDto;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceResponseDto;
import com.smartHomeApp.SmartHomeApp.device.application.mapper.DeviceMapper;
import com.smartHomeApp.SmartHomeApp.device.domain.events.DeviceAddedEvent;
import com.smartHomeApp.SmartHomeApp.device.infrastructure.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;


@Service
public class DeviceService {

  private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

  private final DeviceRepository deviceRepository;
  private final DeviceMapper deviceMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final Optional<DeviceTopicCache> topicCache;

  public DeviceService(DeviceRepository deviceRepository,
                       DeviceMapper deviceMapper,
                       ApplicationEventPublisher eventPublisher,
                       @Nullable Optional<DeviceTopicCache> topicCache) {
    this.deviceRepository = deviceRepository;
    this.deviceMapper = deviceMapper;
    this.eventPublisher = eventPublisher;
    this.topicCache = topicCache == null ? Optional.empty() : topicCache;
  }

  @Transactional
  public DeviceResponseDto addDevice(DeviceRequestDto dto, Long userId) {
    var now = Instant.now();

    var device = deviceMapper.toEntity(dto);

    device.setUserId(userId);
    device.setCreatedAt(now);
    device.setUpdatedAt(now);

    if (deviceRepository.findByDeviceId(device.getDeviceId()).isPresent()) {
      log.warn("Attempt to create device which already exists: {}", device.getDeviceId());
      throw new DeviceExceptions.DeviceAlreadyExistsException(device.getDeviceId());
    }

    var saved = deviceRepository.save(device);
    log.info("Device saved: deviceId={} id={}", saved.getDeviceId(), saved.getId());

    topicCache.ifPresent(c -> {
      try {
        c.put(saved.getTopicPrefix(), saved.getDeviceId());
      } catch (Exception e) {
        log.warn("Failed to update topic cache for {} -> {}: {}", saved.getTopicPrefix(), saved.getDeviceId(), e.getMessage());
      }
    });

    eventPublisher.publishEvent(new DeviceAddedEvent(saved.getDeviceId(), saved.getUserId(), saved.getCreatedAt()));
    log.debug("Published DeviceAddedEvent for deviceId={}", saved.getDeviceId());

    return deviceMapper.toResponse(saved);
  }
}
