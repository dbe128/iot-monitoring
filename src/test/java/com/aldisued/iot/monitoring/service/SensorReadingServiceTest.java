package com.aldisued.iot.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
import com.aldisued.iot.monitoring.repository.SensorReadingRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

  private static final UUID SENSOR_ID = UUID.randomUUID();
  private static final LocalDateTime TIMESTAMP = LocalDateTime.parse("2024-05-01T10:15:30");

  @Mock
  private SensorReadingRepository sensorReadingRepository;

  @Mock
  private SensorRepository sensorRepository;

  @InjectMocks
  private SensorReadingService sensorReadingService;

  @Test
  void savesReadingForExistingSensor() {
    Sensor sensor = new Sensor("Temperature Sensor", SensorType.TEMPERATURE);
    when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
    when(sensorReadingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    SensorReading saved = sensorReadingService.saveSensorReading(
        new SensorReadingDto(SENSOR_ID, 21.5, TIMESTAMP));

    assertThat(saved.getSensor()).isSameAs(sensor);
    assertThat(saved.getValue()).isEqualTo(21.5);
    assertThat(saved.getTimestamp()).isEqualTo(TIMESTAMP);
  }

  @Test
  void throwsWhenSensorDoesNotExist() {
    when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> sensorReadingService.saveSensorReading(
        new SensorReadingDto(SENSOR_ID, 21.5, TIMESTAMP)))
        .isInstanceOf(SensorNotFoundException.class)
        .hasMessageContaining(SENSOR_ID.toString());

    verify(sensorReadingRepository, never()).save(any());
  }
}
