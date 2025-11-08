package com.smartHomeApp.SmartHomeApp.device.application.mapper;

import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceRequestDto;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceResponseDto;
import com.smartHomeApp.SmartHomeApp.device.domain.entity.Device;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper {

  public Device toEntity(DeviceRequestDto dto) {
    return Device.builder()
      .deviceId(dto.deviceId())
      .name(dto.name())
      .type(dto.type())
      .topicPrefix(dto.topicPrefix())
      .userId(dto.userId())
      .location(dto.location())
      .build();
  }

  public DeviceResponseDto toResponse(Device device) {
    return new DeviceResponseDto(
      device.getId(),
      device.getDeviceId(),
      device.getName(),
      device.getType(),
      device.getStatus(),
      device.getLastValue(),
      device.getTopicPrefix(),
      device.getLocation(),
      device.getLastSeen(),
      device.getCreatedAt()
    );
  }
}
