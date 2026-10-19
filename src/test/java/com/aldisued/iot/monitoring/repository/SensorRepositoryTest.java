package com.aldisued.iot.monitoring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aldisued.iot.monitoring.IntegrationTestBase;
import com.aldisued.iot.monitoring.entity.Sensor;
import com.aldisued.iot.monitoring.entity.SensorType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class SensorRepositoryTest extends IntegrationTestBase {

  @Autowired
  private SensorRepository sensorRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @AfterEach
  void cleanup() {
    sensorRepository.deleteAll();
  }

  @Test
  void storesSensorTypeAsString() {
    Sensor sensor = sensorRepository.save(new Sensor("Pressure Sensor", SensorType.ATMOSPHERIC_PRESSURE));

    String storedType = jdbcTemplate.queryForObject(
        "SELECT type FROM sensors WHERE id = ?", String.class, sensor.getId());

    assertThat(storedType).isEqualTo("ATMOSPHERIC_PRESSURE");
  }

  @Test
  void rejectsDuplicateName() {
    sensorRepository.saveAndFlush(new Sensor("Humidity Sensor", SensorType.HUMIDITY));

    assertThatThrownBy(() -> sensorRepository.saveAndFlush(
        new Sensor("Humidity Sensor", SensorType.TEMPERATURE)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void checksExistenceByName() {
    sensorRepository.save(new Sensor("Temperature Sensor", SensorType.TEMPERATURE));

    assertThat(sensorRepository.existsByName("Temperature Sensor")).isTrue();
    assertThat(sensorRepository.existsByName("Unknown Sensor")).isFalse();
  }
}
