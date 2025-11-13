package com.smartHomeApp.SmartHomeApp.device.application.services;

import com.smartHomeApp.SmartHomeApp.auth.infrastructure.security.SecurityUtil;
import com.smartHomeApp.SmartHomeApp.common.exceptions.DeviceExceptions;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceRequestDto;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceResponseDto;
import com.smartHomeApp.SmartHomeApp.device.application.mapper.DeviceMapper;
import com.smartHomeApp.SmartHomeApp.device.domain.events.DeviceAddedEvent;
import com.smartHomeApp.SmartHomeApp.device.infrastructure.cache.DeviceTopicCache;
import com.smartHomeApp.SmartHomeApp.device.infrastructure.repository.DeviceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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
    if (device.getTopicPrefix() != null && deviceRepository.findByTopicPrefix(device.getTopicPrefix()).isPresent()) {
      log.warn("Attempt to create device with topicPrefix already in use: {}", device.getTopicPrefix());
      throw new DeviceExceptions.DeviceAlreadyExistsException(device.getTopicPrefix());
    }

    var saved = device;
    try {
      saved = deviceRepository.save(device);
    } catch (DataIntegrityViolationException ex) {
      log.warn("DataIntegrityViolation when saving device {}: {}", device.getDeviceId(), ex.getMessage());
      throw new DeviceExceptions.DeviceAlreadyExistsException(device.getDeviceId());
    }

    log.info("Device saved: deviceId={} id={}", saved.getDeviceId(), saved.getId());

    var topicPrefix = saved.getTopicPrefix();
    var deviceId = saved.getDeviceId();
    topicCache.ifPresent(c -> {
      try {
        c.put(topicPrefix, deviceId);
      } catch (Exception e) {
        log.warn("Failed to update topic cache for {} -> {}: {}", topicPrefix, deviceId, e.getMessage());
      }
    });

    eventPublisher.publishEvent(new DeviceAddedEvent(saved.getDeviceId(), saved.getUserId(), saved.getCreatedAt()));
    log.debug("Published DeviceAddedEvent for deviceId={}", saved.getDeviceId());

    return deviceMapper.toResponse(saved);
  }
  @Transactional(readOnly = true)
  public List<DeviceResponseDto> getDevicesByUser(Long userId) {
    if (userId == null) {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      try {
        userId = SecurityUtil.getUserId(authentication);
      } catch (IllegalStateException ex) {
        throw new IllegalArgumentException("userId must not be null and could not be resolved from authentication", ex);
      }
    }

    var devices = deviceRepository.findAllByUserId(userId);
    if (devices.isEmpty()) {
      log.warn("No devices found for userId={}", userId);
      throw new DeviceExceptions.DeviceNotFoundException("user:" + userId);
    }

    var dtos = devices.stream()
      .map(deviceMapper::toResponse)
      .toList();
    log.debug("Fetched {} devices for userId={}", dtos.size(), userId);
    return dtos;
  }
}
