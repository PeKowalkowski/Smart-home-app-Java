package com.smartHomeApp.SmartHomeApp.config;

import com.smartHomeApp.SmartHomeApp.config.securityHandler.RestAccessDeniedHandler;
import com.smartHomeApp.SmartHomeApp.config.securityHandler.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/api/**")
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .accessDeniedHandler(new RestAccessDeniedHandler())
        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
      )
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }
  @Bean
  public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/",
          "/css/**", "/js/**", "/images/**", "/webjars/**",
          "/auth/**",
          "/about", "/contact",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
        ).permitAll()
        .anyRequest().authenticated()
      )

      .formLogin(form -> form
        .loginPage("/auth/login")
        .loginProcessingUrl("/auth/login")
        .defaultSuccessUrl("/dashboard", true)
        .permitAll()
      )

      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/")
        .permitAll()
      )

    ;

    return http.build();
  }
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
