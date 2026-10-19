package com.aldisued.iot.monitoring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SensorReadingRepositoryTest extends IntegrationTestBase {

  private static final LocalDateTime FROM = LocalDateTime.parse("2024-05-01T00:00:00");
  private static final LocalDateTime TO = LocalDateTime.parse("2024-05-01T23:59:59");

  @Autowired
  private SensorReadingRepository sensorReadingRepository;

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
    sensorReadingRepository.deleteAll();
    sensorRepository.deleteAll();
  }

  @Test
  void averagesReadingsOfGivenTypeWithinInclusiveRange() {
    save(temperatureSensor, 10.0, FROM);
    save(temperatureSensor, 20.0, TO);
    save(temperatureSensor, 100.0, FROM.minusSeconds(1));
    save(temperatureSensor, 100.0, TO.plusSeconds(1));
    save(humiditySensor, 0.5, FROM.plusHours(1));

    assertThat(sensorReadingRepository.findAverageValue(SensorType.TEMPERATURE, FROM, TO))
        .contains(15.0);
  }

  @Test
  void returnsEmptyAverageWhenNoReadingsInRange() {
    save(temperatureSensor, 10.0, FROM.minusDays(1));
    save(humiditySensor, 0.5, FROM.plusHours(1));

    assertThat(sensorReadingRepository.findAverageValue(SensorType.TEMPERATURE, FROM, TO))
        .isEmpty();
  }

  @Test
  void findsValuesOfGivenTypeWithinInclusiveRangeOrderedByTimestamp() {
    save(temperatureSensor, 3.0, TO);
    save(temperatureSensor, 1.0, FROM);
    save(temperatureSensor, 2.0, FROM.plusHours(12));
    save(temperatureSensor, 100.0, FROM.minusSeconds(1));
    save(temperatureSensor, 100.0, TO.plusSeconds(1));
    save(humiditySensor, 0.5, FROM.plusHours(1));

    assertThat(sensorReadingRepository.findValues(SensorType.TEMPERATURE, FROM, TO))
        .containsExactly(1.0, 2.0, 3.0);
  }

  @Test
  void findsNoValuesWhenNoReadingsInRange() {
    save(humiditySensor, 0.5, FROM.plusHours(1));

    assertThat(sensorReadingRepository.findValues(SensorType.TEMPERATURE, FROM, TO)).isEmpty();
  }

  private void save(Sensor sensor, double value, LocalDateTime timestamp) {
    sensorReadingRepository.save(new SensorReading(value, timestamp, sensor));
  }
}
