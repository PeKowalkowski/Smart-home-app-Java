package com.smartHomeApp.SmartHomeApp.auth.infrastructure.security;

import com.smartHomeApp.SmartHomeApp.user.domain.security.UserPrincipal;
import com.smartHomeApp.SmartHomeApp.user.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.user.infrastructure.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserManagerConfig implements UserDetailsService {

  private final UserRepository userRepository;

  public UserManagerConfig(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    Optional<User> userOptional = userRepository.findByEmail(usernameOrEmail)
      .or(() -> userRepository.findByUsername(usernameOrEmail));

    User loadedUser = userOptional.orElseThrow(() ->
      new UsernameNotFoundException("User not found")
    );

    return new UserPrincipal(loadedUser);
  }
}
