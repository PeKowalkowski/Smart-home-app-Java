package com.smartHomeApp.SmartHomeApp.config;

import com.smartHomeApp.SmartHomeApp.infrastructure.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtFilter;

  @Bean
  @Order(1)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .securityMatcher("/api/**")
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**", "/error").permitAll()
        .requestMatchers("/api/**").authenticated()
        .requestMatchers("/admin/**", "/actuator/**").hasRole("ADMIN")
        .anyRequest().denyAll()
      )
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((request, response, authException) ->
          response.sendError(401, "Unauthorized"))
        .accessDeniedHandler((request, response, accessDeniedException) ->
          response.sendError(403, "Forbidden"))
      );

    return http.build();
  }
  @Bean
  @Order(2)
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
      );
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
