package com.aldisued.iot.monitoring.dto;

import com.aldisued.iot.monitoring.entity.Alert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlertDto(
    @NotNull UUID sensorId,
    @NotBlank String message,
    @NotNull LocalDateTime timestamp
) {

  public static AlertDto from(Alert alert) {
    return new AlertDto(alert.getSensor().getId(), alert.getMessage(), alert.getTimestamp());
  }
}
