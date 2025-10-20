package com.smartHomeApp.SmartHomeApp.config.securityHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartHomeApp.SmartHomeApp.exceptions.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    ProblemDetail pd = GlobalExceptionHandler.createProblemDetail(
      HttpStatus.FORBIDDEN,
      "Forbidden",
      "ACCESS_DENIED",
      "Access denied"
    );

    response.getWriter().write(mapper.writeValueAsString(pd));
    response.getWriter().flush();
  }
}

