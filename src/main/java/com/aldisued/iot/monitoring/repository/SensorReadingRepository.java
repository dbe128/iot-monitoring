package com.aldisued.iot.monitoring.repository;

import com.aldisued.iot.monitoring.entity.SensorReading;
import com.aldisued.iot.monitoring.entity.SensorType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

  @Query("""
      SELECT AVG(r.value) FROM SensorReading r
      WHERE r.sensor.type = :sensorType AND r.timestamp BETWEEN :from AND :to
      """)
  Optional<Double> findAverageValue(SensorType sensorType, LocalDateTime from, LocalDateTime to);
}
