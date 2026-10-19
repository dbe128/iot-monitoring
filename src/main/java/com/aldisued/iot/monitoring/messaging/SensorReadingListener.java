package com.aldisued.iot.monitoring.messaging;

import com.aldisued.iot.monitoring.dto.SensorReadingDto;
import com.aldisued.iot.monitoring.service.SensorReadingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingListener {
  private final SensorReadingService sensorReadingService;

  public SensorReadingListener(SensorReadingService sensorReadingService) {
    this.sensorReadingService = sensorReadingService;
  }

  @KafkaListener(topics = KafkaTopics.SENSOR_READINGS, groupId = "iot-monitoring")
  public void listen(SensorReadingDto sensorReadingDto) {
    sensorReadingService.saveSensorReading(sensorReadingDto);
  }

}
