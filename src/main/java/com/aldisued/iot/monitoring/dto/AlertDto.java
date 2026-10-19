package com.aldisued.iot.monitoring.dto;

import com.aldisued.iot.monitoring.entity.Alert;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertDto(
    UUID sensorId,
    String message,
    LocalDateTime timestamp
) {

  public static AlertDto from(Alert alert) {
    return new AlertDto(alert.getSensor().getId(), alert.getMessage(), alert.getTimestamp());
  }
}
