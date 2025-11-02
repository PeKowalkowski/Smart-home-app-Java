package com.smartHomeApp.SmartHomeApp.user.domain.events;

public record UserLockedEvent(Long userId, String reason) { }
