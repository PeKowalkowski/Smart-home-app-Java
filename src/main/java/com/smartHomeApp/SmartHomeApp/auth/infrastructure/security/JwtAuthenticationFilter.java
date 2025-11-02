package com.smartHomeApp.SmartHomeApp.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtDecoder jwtDecoder;
  private final UserDetailsService userDetailsService;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    try {
      var token = extractTokenFromHeader(request);

      if (token != null) {
        validateAndAuthenticate(token);
      }

    } catch (JwtException ex) {
      log.warn("Invalid JWT token : {}", ex.getMessage());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    } catch (Exception ex) {
      log.error("Error during validation ", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }
    filterChain.doFilter(request, response);
  }


  private String extractTokenFromHeader(HttpServletRequest request) {
    var header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  private void validateAndAuthenticate(String token) {
    Jwt jwt = jwtDecoder.decode(token);
    String username = jwt.getSubject();

    if (username == null) {
      throw new JwtException("Missing subject (username) in token");
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    var authentication = new UsernamePasswordAuthenticationToken(
      userDetails,
      null,
      userDetails.getAuthorities()
    );

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }


}
