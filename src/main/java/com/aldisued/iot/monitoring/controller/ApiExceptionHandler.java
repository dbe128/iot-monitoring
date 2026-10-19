package com.aldisued.iot.monitoring.controller;

import com.aldisued.iot.monitoring.exception.SensorAlreadyExistsException;
import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(SensorNotFoundException.class)
  public ProblemDetail handleSensorNotFound(SensorNotFoundException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(SensorAlreadyExistsException.class)
  public ProblemDetail handleSensorAlreadyExists(SensorAlreadyExistsException exception) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
  }

  // Concurrent inserts can pass the existence check and hit the unique constraint instead
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrityViolation() {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
        "Request conflicts with existing data");
  }
}
