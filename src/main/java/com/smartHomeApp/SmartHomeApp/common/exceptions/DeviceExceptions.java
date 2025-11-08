package com.smartHomeApp.SmartHomeApp.common.exceptions;

import org.springframework.http.HttpStatus;

public class DeviceExceptions extends RuntimeException{
  public DeviceExceptions(String message) { super(message); }

  public static class DeviceAlreadyExistsException extends BusinessExceptions {
    public DeviceAlreadyExistsException(String deviceId) {
      super("Device with id " + deviceId + " already exists: " + deviceId);
    }
  }

}
