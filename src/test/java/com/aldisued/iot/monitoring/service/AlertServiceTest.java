package com.aldisued.iot.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.AlertNotFoundException;
import com.aldisued.iot.monitoring.repository.AlertRepository;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

  private static final UUID SENSOR_ID = UUID.randomUUID();
  private static final LocalDateTime TIMESTAMP = LocalDateTime.parse("2024-05-01T10:15:30");

  @Mock
  private AlertRepository alertRepository;

  @Mock
  private SensorRepository sensorRepository;

  @Mock
  private KafkaTemplate<String, AlertDto> kafkaTemplate;

  @InjectMocks
  private AlertService alertService;

  @Test
  void returnsLatestAlertOfSensor() {
    Sensor sensor = new Sensor("Temperature Sensor", SensorType.TEMPERATURE);
    sensor.setId(SENSOR_ID);
    when(alertRepository.findFirstBySensorIdOrderByTimestampDesc(SENSOR_ID))
        .thenReturn(Optional.of(new Alert("Too hot", TIMESTAMP, sensor)));

    AlertDto alert = alertService.findLastAlertBySensorId(SENSOR_ID);

    assertThat(alert).isEqualTo(new AlertDto(SENSOR_ID, "Too hot", TIMESTAMP));
  }

  @Test
  void throwsWhenSensorHasNoAlert() {
    when(alertRepository.findFirstBySensorIdOrderByTimestampDesc(SENSOR_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> alertService.findLastAlertBySensorId(SENSOR_ID))
        .isInstanceOf(AlertNotFoundException.class)
        .hasMessageContaining(SENSOR_ID.toString());
  }
}
