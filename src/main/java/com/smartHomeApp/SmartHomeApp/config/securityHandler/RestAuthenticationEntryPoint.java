package com.smartHomeApp.SmartHomeApp.config.securityHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHomeApp.SmartHomeApp.exceptions.GlobalExceptionHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper mapper = new ObjectMapper();


  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail pd = GlobalExceptionHandler.createProblemDetail(
      HttpStatus.UNAUTHORIZED,
      "Unauthorized",
      "UNAUTHORIZED",
      "Authentication required"
    );

    response.getWriter().write(mapper.writeValueAsString(pd));
    response.getWriter().flush();
  }
}

