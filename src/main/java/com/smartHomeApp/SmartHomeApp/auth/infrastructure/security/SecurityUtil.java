package com.smartHomeApp.SmartHomeApp.auth.infrastructure.security;

import com.smartHomeApp.SmartHomeApp.user.domain.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtil {

  private SecurityUtil() {}

  public static Long getUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    var principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal up) {
      return up.getId();
    }

    if (principal instanceof Jwt jwt) {
      String userIdStr = jwt.getClaimAsString("user_id");
      if (userIdStr == null) userIdStr = jwt.getSubject();
      try {
        return Long.valueOf(userIdStr);
      } catch (Exception e) {
        throw new IllegalStateException("Cannot parse user id from jwt: " + userIdStr, e);
      }
    }

    if (principal instanceof UserDetails ud) {
      throw new IllegalStateException("Unsupported UserDetails principal for extracting id. Use UserPrincipal.");
    }

    throw new IllegalStateException("Unsupported principal type for extracting user id: " + principal.getClass().getName());
  }
}
