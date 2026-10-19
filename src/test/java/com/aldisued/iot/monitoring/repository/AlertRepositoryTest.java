package com.aldisued.iot.monitoring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.entity.Alert;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AlertRepositoryTest extends IntegrationTestBase {

  private static final LocalDateTime NOON = LocalDateTime.parse("2024-05-01T12:00:00");

  @Autowired
  private AlertRepository alertRepository;

  @Autowired
  private SensorRepository sensorRepository;

  private Sensor temperatureSensor;
  private Sensor humiditySensor;

  @BeforeEach
  void setup() {
    temperatureSensor = sensorRepository.save(
        new Sensor("Temperature Sensor", SensorType.TEMPERATURE));
    humiditySensor = sensorRepository.save(new Sensor("Humidity Sensor", SensorType.HUMIDITY));
  }

  @AfterEach
  void cleanup() {
    alertRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void findsLatestAlertOfGivenSensorOnly() {
    alertRepository.save(new Alert("older", NOON.minusHours(1), temperatureSensor));
    alertRepository.save(new Alert("latest", NOON, temperatureSensor));
    alertRepository.save(new Alert("other sensor", NOON.plusHours(1), humiditySensor));

    assertThat(alertRepository.findFirstBySensorIdOrderByTimestampDesc(temperatureSensor.getId()))
        .map(Alert::getMessage)
        .contains("latest");
  }

  @Test
  void returnsEmptyWhenSensorHasNoAlert() {
    alertRepository.save(new Alert("other sensor", NOON, humiditySensor));

    assertThat(alertRepository.findFirstBySensorIdOrderByTimestampDesc(temperatureSensor.getId()))
        .isEmpty();
  }
}
