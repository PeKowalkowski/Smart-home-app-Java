package com.smartHomeApp.SmartHomeApp.config.userConfig;

import com.smartHomeApp.SmartHomeApp.domain.entity.User;
import com.smartHomeApp.SmartHomeApp.infrastructure.db.repository.UserRepository;
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
