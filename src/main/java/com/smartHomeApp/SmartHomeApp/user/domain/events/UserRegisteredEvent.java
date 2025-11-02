package com.smartHomeApp.SmartHomeApp.user.domain.events;

public record UserRegisteredEvent(Long userId, String email) { }
