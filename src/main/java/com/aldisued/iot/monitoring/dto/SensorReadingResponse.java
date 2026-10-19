package com.aldisued.iot.monitoring.dto;

import com.aldisued.iot.monitoring.entity.SensorReading;
import java.time.LocalDateTime;
import java.util.UUID;

public record SensorReadingResponse(
    Long id,
    UUID sensorId,
    Double value,
    LocalDateTime timestamp
) {

  public static SensorReadingResponse from(SensorReading sensorReading) {
    return new SensorReadingResponse(
        sensorReading.getId(),
        sensorReading.getSensor().getId(),
        sensorReading.getValue(),
        sensorReading.getTimestamp()
    );
  }
}
