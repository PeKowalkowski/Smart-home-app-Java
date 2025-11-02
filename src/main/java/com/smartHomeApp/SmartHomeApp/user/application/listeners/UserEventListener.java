package com.smartHomeApp.SmartHomeApp.user.application.listeners;

import com.smartHomeApp.SmartHomeApp.user.domain.events.UserRegisteredEvent;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserLockedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventListener {

  @EventListener
  public void onUserRegistered(UserRegisteredEvent ev) {
    log.info("[EVENT] UserRegistered userId={} email={}", ev.userId(), ev.email());
    // TODO: call EmailService to send welcome mail
  }

  @EventListener
  public void onUserLocked(UserLockedEvent ev) {
    log.warn("[EVENT] UserLocked userId={} reason={}", ev.userId(), ev.reason());
    // TODO: notify admin / audit
  }
}

