package com.smartHomeApp.SmartHomeApp.device.api.controllers;


import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceRequestDto;
import com.smartHomeApp.SmartHomeApp.device.application.dto.DeviceResponseDto;
import com.smartHomeApp.SmartHomeApp.device.application.services.DeviceService;
import com.smartHomeApp.SmartHomeApp.user.domain.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class DeviceController {

  private final DeviceService deviceService;

  public DeviceController(DeviceService deviceService) {
    this.deviceService = deviceService;
  }

  @PostMapping("/devices")
  public ResponseEntity<DeviceResponseDto> addDeviceForCurrentUser(
    @Valid @RequestBody DeviceRequestDto dto,
    @AuthenticationPrincipal UserPrincipal principal
  ) {
    Long userId = principal != null ? principal.getId() : null;
    var created = deviceService.addDevice(dto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/users/{userId}/devices")
  public ResponseEntity<DeviceResponseDto> addDeviceForUser(
    @PathVariable Long userId,
    @Valid @RequestBody DeviceRequestDto dto
  ) {
    var created = deviceService.addDevice(dto, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/devices/me")
  public ResponseEntity<List<DeviceResponseDto>> getMyDevices(@AuthenticationPrincipal UserPrincipal principal) {
    Long userId = principal != null ? principal.getId() : null;
    var dtos = deviceService.getDevicesByUser(userId);
    return ResponseEntity.ok(dtos);
  }


  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/users/{userId}/devices")
  public ResponseEntity<List<DeviceResponseDto>> getDevicesByUserId(@PathVariable Long userId) {
    var dtos = deviceService.getDevicesByUser(userId);
    return ResponseEntity.ok(dtos);
  }

}
