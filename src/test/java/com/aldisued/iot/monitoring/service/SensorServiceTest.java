package com.aldisued.iot.monitoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aldisued.iot.monitoring.dto.SensorDto;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import com.aldisued.iot.monitoring.exception.SensorAlreadyExistsException;
import com.aldisued.iot.monitoring.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

  @Mock
  private SensorRepository sensorRepository;

  @InjectMocks
  private SensorService sensorService;

  @Test
  void savesNewSensor() {
    when(sensorRepository.existsByName("Temperature Sensor")).thenReturn(false);
    when(sensorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    Sensor saved = sensorService.saveSensor(
        new SensorDto("Temperature Sensor", SensorType.TEMPERATURE));

    assertThat(saved.getName()).isEqualTo("Temperature Sensor");
    assertThat(saved.getType()).isEqualTo(SensorType.TEMPERATURE);
  }

  @Test
  void rejectsDuplicateName() {
    when(sensorRepository.existsByName("Temperature Sensor")).thenReturn(true);

    assertThatThrownBy(() -> sensorService.saveSensor(
        new SensorDto("Temperature Sensor", SensorType.TEMPERATURE)))
        .isInstanceOf(SensorAlreadyExistsException.class)
        .hasMessageContaining("Temperature Sensor");

    verify(sensorRepository, never()).save(any());
  }
}
