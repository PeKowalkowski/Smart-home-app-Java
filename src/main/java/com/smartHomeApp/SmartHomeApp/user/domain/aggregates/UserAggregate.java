package com.smartHomeApp.SmartHomeApp.user.domain.aggregates;

import com.smartHomeApp.SmartHomeApp.user.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserLockedEvent;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserLoggedInEvent;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserRegisteredEvent;
import com.smartHomeApp.SmartHomeApp.user.domain.events.UserPasswordChangedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserAggregate {

  private final User user;
  private final List<Object> domainEvents = new ArrayList<>();

  private static final int DEFAULT_MAX_FAILED_ATTEMPTS = 5;
  private final int maxFailedAttempts;

  public UserAggregate(User user) {
    this(user, DEFAULT_MAX_FAILED_ATTEMPTS);
  }

  public UserAggregate(User user, int maxFailedAttempts) {
    this.user = Objects.requireNonNull(user, "user cannot be null");
    this.maxFailedAttempts = maxFailedAttempts;
    if (this.user.getFailedLoginAttempts() == null) {
      this.user.setFailedLoginAttempts(0);
    }
    if (this.user.getLocked() == null) {
      this.user.setLocked(false);
    }
  }


  public static UserAggregate registerNew(User newUser) {
    newUser.setActive(true);
    newUser.setRegistrationDate(LocalDateTime.now());
    newUser.setFailedLoginAttempts(0);
    newUser.setLocked(false);
    newUser.setUpdatedAt(LocalDateTime.now());
    var agg = new UserAggregate(newUser);
    agg.domainEvents.add(new UserRegisteredEvent(newUser.getId(), newUser.getEmail()));
    return agg;
  }

  public void recordLogin() {
    user.setLastLogin(LocalDateTime.now());
    user.setFailedLoginAttempts(0);
    user.setUpdatedAt(LocalDateTime.now());
    domainEvents.add(new UserLoggedInEvent(user.getId(), user.getEmail(), user.getLastLogin()));
  }

  public void onFailedLoginAttempt() {
    Integer current = user.getFailedLoginAttempts();
    if (current == null) current = 0;
    current = current + 1;
    user.setFailedLoginAttempts(current);
    user.setUpdatedAt(LocalDateTime.now());

    if (current >= maxFailedAttempts && !Boolean.TRUE.equals(user.getLocked())) {
      user.setLocked(true);
      domainEvents.add(new UserLockedEvent(user.getId(), "TOO_MANY_FAILED_ATTEMPTS"));
    }
  }

  public void changePassword(String hashedPassword) {
    user.setPassword(hashedPassword);
    user.setUpdatedAt(LocalDateTime.now());
    domainEvents.add(new UserPasswordChangedEvent(user.getId(), user.getEmail(), LocalDateTime.now()));
  }

  public void lock(String reason) {
    if (!Boolean.TRUE.equals(user.getLocked())) {
      user.setLocked(true);
      user.setUpdatedAt(LocalDateTime.now());
      domainEvents.add(new UserLockedEvent(user.getId(), reason));
    }
  }

  public void unlock() {
    if (Boolean.TRUE.equals(user.getLocked())) {
      user.setLocked(false);
      user.setFailedLoginAttempts(0);
      user.setUpdatedAt(LocalDateTime.now());
    }
  }

  public User getUser() {
    return user;
  }

  public List<Object> pullDomainEvents() {
    var copy = List.copyOf(domainEvents);
    domainEvents.clear();
    return copy;
  }
}
