package com.aldisued.iot.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aldisued.iot.monitoring.dto.AlertDto;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.AlertNotFoundException;
import com.aldisued.iot.monitoring.exception.SensorNotFoundException;
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
  void savesAlertAndPublishesIt() {
    Sensor sensor = new Sensor("Temperature Sensor", SensorType.TEMPERATURE);
    AlertDto alertDto = new AlertDto(SENSOR_ID, "Too hot", TIMESTAMP);
    when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.of(sensor));
    when(alertRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Alert saved = alertService.saveAlert(alertDto);

    assertThat(saved.getSensor()).isSameAs(sensor);
    assertThat(saved.getMessage()).isEqualTo("Too hot");
    assertThat(saved.getTimestamp()).isEqualTo(TIMESTAMP);
    verify(kafkaTemplate).send("alerts", alertDto);
  }

  @Test
  void neitherSavesNorPublishesAlertOfUnknownSensor() {
    when(sensorRepository.findById(SENSOR_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> alertService.saveAlert(new AlertDto(SENSOR_ID, "Too hot", TIMESTAMP)))
        .isInstanceOf(SensorNotFoundException.class);

    verify(alertRepository, never()).save(any());
    verifyNoInteractions(kafkaTemplate);
  }

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
