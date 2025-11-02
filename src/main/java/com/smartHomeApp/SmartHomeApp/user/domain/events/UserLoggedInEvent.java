package com.smartHomeApp.SmartHomeApp.user.domain.events;

import java.time.LocalDateTime;

public record UserLoggedInEvent(Long userId, String email, LocalDateTime when) { }
