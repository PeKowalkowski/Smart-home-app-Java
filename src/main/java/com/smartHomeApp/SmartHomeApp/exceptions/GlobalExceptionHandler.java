package com.smartHomeApp.SmartHomeApp.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
   var fieldErrors = ex.getBindingResult().getFieldErrors();

   String detail = fieldErrors.stream()
     .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
     .collect(Collectors.joining("; "));

   var errorsMap = fieldErrors.stream()
     .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a + "; " + b));

   ProblemDetail pd = createProblemDetail(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_FAILED", detail);
   pd.setProperty("validationErrors", errorsMap);

   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
 }
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ProblemDetail> handleBusinessExceptions(RuntimeException ex) {
    ProblemDetail problemDetail = switch (ex) {
      case BusinessExceptions.UserAlreadyExistsException userAlreadyExistsException -> createProblemDetail(
        HttpStatus.CONFLICT,
        "User already exists",
        "USER_ALREADY_EXISTS",
        userAlreadyExistsException.getMessage()
      );
      case BusinessExceptions.InvalidCredentialsException invalidCredentialsException -> createProblemDetail(
        HttpStatus.UNAUTHORIZED,
        "Invalid credentials",
        "AUTH_INVALID_CREDENTIALS",
        invalidCredentialsException.getMessage()
      );
      default -> createProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Unexpected error",
        "UNEXPECTED_ERROR",
        ex.getMessage() != null ? ex.getMessage() : "Unexpected error"
      );
    };

    log.error("Exception handled: [{}] {}", ex.getClass().getSimpleName(), ex.getMessage());
    return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
  }
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Data integrity violation: {}", ex.getMessage());
    var problemDetail = createProblemDetail(HttpStatus.CONFLICT, "Data conflict", "DATA_INTEGRITY_VIOLATION",
      "Resource conflict, possibly duplicate key");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleAll(Exception ex) {
    log.error("Unhandled exception", ex);
    var problemDetail = createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error", "UNEXPECTED_ERROR", "Unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
  }


  public static ProblemDetail createProblemDetail(HttpStatus status, String title, String errorCode, String detail) {
    var problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setTitle(title);
    problemDetail.setProperty("errorCode", errorCode);
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail; }
}


