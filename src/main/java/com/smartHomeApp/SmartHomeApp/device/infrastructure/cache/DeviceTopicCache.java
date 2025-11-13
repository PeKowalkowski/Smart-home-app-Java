package com.smartHomeApp.SmartHomeApp.device.infrastructure.cache;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceTopicCache {
  private final Map<String, String> cache = new ConcurrentHashMap<>();

  public void put(String topicPrefix, String deviceId) {
    cache.put(topicPrefix, deviceId);
  }

  public Optional<String> get(String topicPrefix) {
    return Optional.ofNullable(cache.get(topicPrefix));
  }

  public void remove(String topicPrefix) {
    cache.remove(topicPrefix);
  }
}
