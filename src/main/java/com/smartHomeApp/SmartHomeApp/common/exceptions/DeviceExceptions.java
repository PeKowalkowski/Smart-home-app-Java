package com.smartHomeApp.SmartHomeApp.common.exceptions;


public class DeviceExceptions extends RuntimeException{
  public DeviceExceptions(String message) { super(message); }

  public static class DeviceAlreadyExistsException extends DeviceExceptions {
    public DeviceAlreadyExistsException(String deviceId) {
      super("Device with id " + deviceId + " already exists: " + deviceId);
    }
  }

  public static class DeviceNotFoundException extends DeviceExceptions {
    public DeviceNotFoundException(String deviceId) {
      super("Device with id " + deviceId + " not found: " + deviceId);
    }
  }
  public static class DeviceCommandRejectedException extends DeviceExceptions {
    public DeviceCommandRejectedException(String deviceId, String reason) {
      super("Command rejected for device " + deviceId + ": " + reason);
    }
  }

}
